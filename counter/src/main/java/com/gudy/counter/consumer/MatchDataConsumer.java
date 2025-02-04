package com.gudy.counter.consumer;

import com.google.common.collect.ImmutableMap;
import com.gudy.counter.config.CounterConfig;

import com.gudy.counter.service.OrderService;
import com.gudy.counter.service.PosiService;
import com.gudy.counter.service.TradeService;
import com.gudy.counter.service.UserService;
import com.gudy.counter.thirdpart.hq.MatchData;
import com.gudy.counter.thirdpart.order.OrderCmd;
import com.gudy.counter.thirdpart.order.OrderStatus;
import com.gudy.counter.util.IDConverter;
import com.gudy.counter.util.JsonUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gudy.counter.config.WebSocketConfig.ORDER_NOTIFY_ADDR_PREFIX;
import static com.gudy.counter.config.WebSocketConfig.TRADE_NOTIFY_ADDR_PREFIX;
import static com.gudy.counter.consumer.MQTTBusConsumer.INNER_MATCH_DATA_ADDR;
import static com.gudy.counter.thirdpart.order.OrderDirection.BUY;
import static com.gudy.counter.thirdpart.order.OrderDirection.SELL;
import static com.gudy.counter.thirdpart.order.OrderStatus.*;

/**
 * match data consumer,它的功能就相对要复杂一些了。
 * 在收到委托变动的数据之后,需要对成交委托的数据做一些业务方面的处理。
 * 然后,还要给订阅的中端发送一个通知。
 * 在match data 当中,它要缓存的数据,就是每一笔从委托中端报上来的委托。
 */
@Log4j2
@Component
public class MatchDataConsumer {

    public static final String ORDER_DATA_CACHE_ADDR = "order_data_cache_addr";

    @Resource
    private PosiService posiService;

    @Resource
    private UserService userService;

    @Resource
    private TradeService tradeService;

    @Resource
    private OrderService orderService;

    @Resource
    private CounterConfig config;

    // <委托编号，OrderCmd>
    // 它的key 就是每一笔委托的那个委托编号。
    // value 就是那笔委托生成的OrderCommand。
    // 这里要注意的是这个委托编号并不是存到数据故里面的那笔委托的ID。
    // 这笔委托编号,它其实已经包含了柜台的那个ID了。
    // 是一个长整型的类型。

    // TODO 这个map的存放逻辑是在orderService里面
    // 委托终端报的那笔委托,
    // 会通过SendOrder的方法,给它发送到网关去。
    // 所以,在发送给网关之前,我们就要先把这笔委托,给它存到这个缓存当中。
    private LongObjectHashMap<OrderCmd> oidOrderMap = new LongObjectHashMap<>();


    @PostConstruct
    private void init() {
        // 和market data consumer 一样,它也是需要处理某一个地址上的数据
        EventBus eventBus = config.getVertx().eventBus();

        //接收委托缓存(从终端来)
        eventBus.consumer(ORDER_DATA_CACHE_ADDR)
                .handler(buffer -> {
                    Buffer body = (Buffer) buffer.body();
                    try {
                        OrderCmd om = config.getBodyCodec().deserialize(body.getBytes(), OrderCmd.class);
                        log.info("cache order:{}", om);
                        oidOrderMap.put(om.oid, om);
                    } catch (Exception e) {
                        log.error(e);
                    }
                });


        eventBus.consumer(INNER_MATCH_DATA_ADDR)
                .handler(buffer -> {
                    //数据长度判断
                    Buffer body = (Buffer) buffer.body();
                    if (body.length() == 0) {
                        return;
                    }

                    MatchData[] matchDataArr = null;
                    try {
                        // 解析获得的这个数据
                        // 数据的类型已经不是market data 了,而是match data。
                        matchDataArr = config.getBodyCodec().deserialize(body.getBytes(), MatchData[].class);
                    } catch (Exception e) {
                        log.error(e);
                    }

                    if (ArrayUtils.isEmpty(matchDataArr)) {
                        return;
                    }
                    // 数组里面的数据可能会存在一种情况
                    // 同一笔委托可能会有多笔match data分布在这些数据里面
                    // 需要对这个数据给它按照委托编号进行一个分类
                    // 规列完之后才好进行下一步的处理
                    // 按照oid进行分类
                    Map<Long, List<MatchData>> collect = Arrays.asList(matchDataArr)
                            .stream().collect(Collectors.groupingBy(t -> t.oid));

                    // 我们按照委托编号给它进行一个遍历
                    for (Map.Entry<Long, List<MatchData>> entry : collect.entrySet()) {
                        // 它 = null,就直接把这个委托编号的数据全都跳过
                        // 这里只是做了一个预防性的编码
                        if (CollectionUtils.isEmpty(entry.getValue())) {
                            continue;
                        }
                        // 根据这个委托号所有的match data 对数据库的数据做一些修改了
                        // 因为数据库的那个组件存的是一个整形的委托ID,那个委托ID是不包含柜台号的
                        // 拆分成柜台编号和柜台里面的那笔委托ID。
                        long oid = entry.getKey();
                        int counterOId = IDConverter.seperateLong2Int(oid)[1];//高位 柜台  低位 内部ID
                        // 修改数据库和通知委托中端
                        updateAndNotify(counterOId, entry.getValue(), oidOrderMap.get(oid));
                    }
                });
    }

    /**
     * 修改数据库和通知委托中端
     *
     *
     * 撮合数据的交互。
     * 撮合数据的变动是柜台,主动推给委托中端的。
     * 这个推送的逻辑,就适合放在MatchDataConsumer#updateAndNotify里面。
     * 通知客户端的内容有两种:
     * 一个是,如果有成交了,我们就要通知委托中端。
     * 另外一个,就是有委托变化了,我们也需要通知委托中端。
     * @param counterOId  柜台内部的委托id
     * @param value 所有的matchdata
     * @param orderCmd 原始的委托指令
     */
    private void updateAndNotify(int counterOId, List<MatchData> value, OrderCmd orderCmd) {
        if (CollectionUtils.isEmpty(value)) {
            return;
        }
        // 这个match data里面可能会包含两种类型的数据,分别是成交跟委托的变动。
        // 针对这两种数据,我们是需要分别处理的。
        //成交
        for (MatchData md : value) {
            // 因为成交数据完全是新的。
            // 所以这部分数据最重要的逻辑就是要把它存到数据库里面去。
            // 而且对于同一笔委托如果有多个match data
            // 也就是说如果有多笔成交数据
            // 这些成交数据是都要存下来的。
            OrderStatus status = md.status;
            if (status == TRADE_ED || status == PART_TRADE) {
                // 如果这笔是部分成交或者全部成交
                // 需要把这笔给它单独存到数据库里面去
                // 更新成交
                tradeService.saveTrade(counterOId, md, orderCmd);
                // 存完数据就要对当前的齿仓资金进行一个多退少补
                // 这种多退少补是要根据买卖方向来进行一个判断的

                // 如果是买单,那它资金和齿仓会发生什么变化呢?
                // 买单,它的持仓一定是要增加的, 所以就需要增加持仓。
                // 同时,它的资金也有可能发生变动。
                // 因为什么呢?
                // 如果你是一个买单,然后是用13块钱买了平安银行,买了30股。
                // 其中有一笔部分成交的单子是10块钱成交了10股。
                // 那么在这个13块当中有10股冻结的那个资金就需要做个释放。
                // 因为你本来期望是用13块钱买着10股的。
                // 结果最后只要10块钱买了,所以你冻结的那个130块是要释放一部分资金出来的。
                // 释放的资金量就是成交的股数乘以你的委托价和成交价的这个差值。

                // 有没有可能我的command的price就是你委托的那个价格小于成交价呢?
                // 那种情况是不可能发生的。
                // 你想你13块钱委托买30股,你是不可能15块钱成交的。
                // 那种情况是有备于常识的。
                if (orderCmd.direction == BUY) {
                    //B 13 30股    10 10股
                    if (orderCmd.price > md.price) {
                        userService.addBalance(orderCmd.uid, (orderCmd.price - md.price) * md.volume);
                    }
                    posiService.addPosi(orderCmd.uid, orderCmd.code, md.volume, md.price);
                } else if (orderCmd.direction == SELL) {
                    // 卖成交就相对好做的。
                    // 只要你卖出了,就会获得相应的资金。
                    // 持仓的变化已经在这一笔委托单发出去的时候做了改变。
                    // 所以这里只需要对资金做处理。
                    // 你获得的资金量就是这笔成交的成交价乘以这笔成交的成交量。
                    // 这里要强调的一点是,我们这里所有的成交都是没有考虑税费还有佣金计算的。
                    // 因为资金清算这一块,它并不是我们这个课程的主要内容。
                    // 所以那一块内容在这里就没有详细的展开。
                    userService.addBalance(orderCmd.uid, md.price * md.volume);
                } else {
                    log.error("wrong direction[{}]", orderCmd.direction);
                }
                //通知客户端

                // 发生成交时候的,主动的推送。
                // 推送是使用publish的。
                // 里面的参数一共有两个。
                // 第一个,是我要推送的数据的那个地址。
                // 推送的地址,就是一个成交通知的前綴,再加上那个委托中端登录的账号。
                // 要推送过去的数据,就是那笔成交。
                // 成交的内容,包括股票代码,方向,还有量。
                // 这里要注意量这个值,实际的成交量,是在match data当中的。
                // 我们不能用order command的那个委托量。
                config.getVertx().eventBus().publish(
                        TRADE_NOTIFY_ADDR_PREFIX + orderCmd.uid,
                        JsonUtil.toJson(
                                ImmutableMap.of("code", orderCmd.code,
                                        "direction", orderCmd.direction,
                                        "volume", md.volume)
                        )
                );

            }
        }
        // 委托变动
        // 一笔委托,可能会有多个委托状态。
        // 然后在撮合核心那边, 这些委托状态肯定都是按照实序依次生成的。
        // 所以我们这里只需要处理整个Match Data的最后一笔委托就可以了。
        // 之前所有的委托, 它的状态的变动即使你更新到数据库,
        // 最后一笔委托还是会把之前的状态给覆盖掉的, 所以我们只需要处理最后一笔就可以了。
        // 根据 最后一笔Match  处理委托
        MatchData finalMatchData = value.get(value.size() - 1);
        OrderStatus finalOrderStatus = finalMatchData.status;
        orderService.updateOrder(orderCmd.uid, counterOId, finalOrderStatus);
        // 我们需要对撤单的这种情况做一个特殊的处理。
        // 因为如果最后一笔是撤单的话,它可能要涉及到资金还有持仓的变动。
        // 如果最后一笔是全撤或者部分撤,我们就要来修改资金跟持仓了。
        // 那既然最后一笔有测单的动作了,委托的缓存里面,就不需要再存放这一笔数据了,我们先把它移除掉。
        // 然后根据不同方向对这笔委托做一个处理。
        if (finalOrderStatus == CANCEL_ED || finalOrderStatus == PART_CANCEL) {
            oidOrderMap.remove(orderCmd.oid);
            //	如果这笔撤单是撤的买单,就需要把冻结的资金给释放出来。
            //	因为你在委托买的时候会冻结一部分资金。
            //	撤掉之后,是要把这部分资金给它做个释放的。
            //	要释放的资金量,就是你的委托价格乘以最终撤单的那个数量。
            //	这里为什么要增加一个负号呢?
            //	是因为对于撤单这种行为,
            //	在撮合核心发布的那个Match Data当中,它的成交量是一个负数。
            //	所以我们这里需要用一个负号,把它那个负数给抵消掉。
            if (orderCmd.direction == BUY) {
                //撤买
                userService.addBalance(orderCmd.uid, -(orderCmd.price * finalMatchData.volume));
            } else if (orderCmd.direction == SELL) {
                // 增加持仓  撤卖单
                // 如果这笔测单测的是一个卖单呢?
                // 那么就需要释放被冻结的持仓。

                // 和上面的情况类似,
                // 我们这里也需要用一个负号,抵消撮合核心发过来的那个负值的数据。
                posiService.addPosi(orderCmd.uid, orderCmd.code, -finalMatchData.volume, orderCmd.price);
            } else {
                log.error("wrong direction[{}]", orderCmd.direction);
            }
        }

        // 通知委托终端(前端项目)
        // 我们再来看委托的通知。
        // 委托的通知,那就是真的只给一个通知了。
        // 具体的数据,是委托中端通过查询的接口获得的。
        // 服务端对于错误的数据,还有行情的这个数据,已经提供了查询,还有推送的功能了。
        config.getVertx().eventBus().publish(ORDER_NOTIFY_ADDR_PREFIX + orderCmd.uid, "");

    }
}
