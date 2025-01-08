package com.gudy.counter.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gudy.counter.bean.Order;
import com.gudy.counter.cache.CacheType;
import com.gudy.counter.cache.RedisCache;
import com.gudy.counter.config.CounterConfig;
import com.gudy.counter.config.GatewayConn;
import com.gudy.counter.mapper.OrderMapper;
import com.gudy.counter.service.OrderService;
import com.gudy.counter.service.PosiService;
import com.gudy.counter.service.UserService;
import com.gudy.counter.thirdpart.bean.MsgConstants;
import com.gudy.counter.thirdpart.order.*;
import com.gudy.counter.util.IDConverter;
import com.gudy.counter.util.JsonUtil;
import io.vertx.core.buffer.Buffer;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:37
 */
@Service
@Log4j2
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Resource
    private CounterConfig config;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private UserService userService;
    @Resource
    private PosiService posiService;
    @Resource
    private GatewayConn gatewayConn;

    List<Order> queryOrder(Long uid) {
        return orderMapper.queryOrder(uid);
    }

    @Override
    public List<Order> getOrderList(long uid) {
        //查缓存
        String suid = Long.toString(uid);
        String orderS = RedisCache.get(suid, CacheType.ORDER);
        if (StringUtils.isEmpty(orderS)) {
            List<Order> tmp = queryOrder(uid);
            List<Order> result = CollectionUtils.isEmpty(tmp) ? Lists.newArrayList() : tmp;
            //更新缓存
            RedisCache.cache(suid, JsonUtil.toJson(result), CacheType.ORDER);
            return result;
        } else {
            //查到 命中缓存
            return JsonUtil.fromJsonArr(orderS, Order.class);
        }
    }

    @Override
    public void updateOrder(long uid, int oid, OrderStatus status) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("Id", oid);
        param.put("Status", status.getCode());
        UpdateWrapper<Order> orderUpdateWrapper = new UpdateWrapper<>();
        orderUpdateWrapper.eq("uid", uid);
        orderUpdateWrapper.set("id", oid);
        orderUpdateWrapper.set("status", status.getCode());
        orderMapper.update(orderUpdateWrapper);
        // 移除缓存
        RedisCache.remove(Long.toString(uid), CacheType.ORDER);
    }

    @Override
    public int saveOrder(OrderCmd orderCmd) {

        Order order = new Order();

        order.setUid(orderCmd.uid);
        order.setCode(orderCmd.code);
        order.setDirection(orderCmd.direction.getDirection());
        order.setType(orderCmd.orderType.getType());
        order.setPrice(orderCmd.price);
        // 委托量
        order.setOrderCount(orderCmd.volume);
        // 委托状态在撮合核心之前都是未报
        order.setStatus(OrderStatus.NOT_SET.getCode());
        // 成交量 这个委托刚到柜台 所以成交量是0
        //order.setTradeCount(0);
        // 委托日期
        order.setOrderDate(LocalDateTime.now());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        //order.setTCount(0)
        int insert = orderMapper.insert(order);
        //判断是否成功
        if (insert > 0) {
            //return Integer.parseInt(param.get("ID").toString());
            // 返回主键
            return order.getId();
        } else {
            return -1;
        }
    }

    @Override
    public boolean sendOrder(long uid, short type, long timestamp, int code,
                             byte direction, long price, long volume, int ordertype) {
        final OrderCmd orderCmd = OrderCmd.builder()
                .type(CmdType.of(type))
                .timestamp(timestamp)
                .mid(config.getMemId())
                .uid(uid)
                .code(code)
                .direction(OrderDirection.of(direction))
                .price(price)
                .volume(volume)
                .orderType(OrderType.of(ordertype))
                .build();

        // 1.入库
        int oid = saveOrder(orderCmd);
        if (oid < 0) {
            return false;
        } else { // TODO 成功的话就发送给网关
            // 1.调整资金持仓数据 TODO 判断是 买 还是 卖
            if (orderCmd.direction == OrderDirection.BUY) {// 买 => 减少资金
                // 减少资金
                userService.minusBalance(orderCmd.uid, orderCmd.price * orderCmd.volume);
            } else if (orderCmd.direction == OrderDirection.SELL) {// 卖 => 减少持仓
                // 减少持仓
                posiService.minusPosi(orderCmd.uid, orderCmd.code, orderCmd.volume, orderCmd.price);
            } else {// TODO 如果 委托 既不是买 也不是卖 那么就是非法
                log.error("wrong direction[{}],ordercmd:{}", orderCmd.direction, orderCmd);
                return false;
            }

            // TODO 2.生成全局ID  组装ID long [柜台ID,  委托ID]
            //  因为一个网关 会连接n个柜台
            //  但是撮合核心撮合完成之后 需要通知柜台 ，通过柜台ID
            // TODO 柜台看到的委托ID 和撮合核心的看到的委托ID 不是同一个
            orderCmd.oid = IDConverter.combineInt2Long(config.getMemId(), oid);

            // 保存委托到缓存
            byte[] serialize = null;
            try {
                serialize = config.getBodyCodec().serialize(orderCmd);
            } catch (Exception e) {
                log.error(e);
            }
            if (serialize == null) {
                return false;
            }
            //config.getVertx().eventBus().send(ORDER_DATA_CACHE_ADDR, Buffer.buffer(serialize));
            // 3.打包委托
            // TODO 先把 ordercmd 包装成 网关提供的消息类 commonMsg ,再把commonMsg变成tcp数据流
            // 4.发送数据
            gatewayConn.sendOrder(orderCmd);
            log.info(orderCmd);
            return true;
        }
    }

    @Override
    public boolean cancelOrder(int uid, int counteroid, int code) {
               /* final OrderCmd orderCmd = OrderCmd.builder()
                .uid(uid)
                .code(code)
                .type(CmdType.CANCEL_ORDER)
                .oid(IDConverter.combineInt2Long(config.getId(), counteroid))
                .build();

        log.info("recv cancel order :{}", orderCmd);

        gatewayConn.sendOrder(orderCmd);*/
        return false;
    }
}

