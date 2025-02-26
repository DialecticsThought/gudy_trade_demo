package com.gudy.engine.bean.orderbook;

import com.google.common.collect.Lists;
import com.gudy.engine.bean.command.CmdResultCode;
import com.gudy.engine.bean.command.RingBufferCmd;
import com.gudy.engine.thirdpart.hq.L1MarketData;
import com.gudy.engine.thirdpart.order.OrderDirection;
import com.gudy.engine.thirdpart.order.OrderStatus;
import io.netty.util.collection.LongObjectHashMap;
import jnr.ffi.annotations.In;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @Description
 * TODO 每一个股票(根据股票代码) 都有一个该对象
 * @Author veritas
 * @Data 2025/1/11 10:31
 */
@Log4j2
@Data
public class GOrderBookImpl implements IOrderBook {
    /**
     * 每一个股票代码都有对应的订单簿
     */
    private int code;

    /**
     * 一个orderBook 包含了若干个orderBucket
     * 订单簿分成两种 卖单 和买单
     * 两个 map == <价格,orderbucket>
     * sellBuckets是从小 -> 大
     * buyBuckets是从大 -> 小
     */
    private final NavigableMap<Long, IOrderBucket> sellBuckets = new TreeMap();

    private final NavigableMap<Long, IOrderBucket> buyBuckets = new TreeMap(Collections.reverseOrder());

    /**
     * 存放所有委托的缓存 用来快速查找
     */
    private final LongObjectHashMap<Order> oidMap = new LongObjectHashMap<>();

    public GOrderBookImpl(int code) {
        this.code = code;
    }

    @Override
    public CmdResultCode newOrder(RingBufferCmd cmd) {
        // 判断新来的订单是否重复
        if (oidMap.containsKey(cmd.oid)) {
            return CmdResultCode.DUPLICATE_ORDER_ID;
        }
        // 生成一个新的订单委托
        // 生成order对象前预处理 因为 这笔订单过来的时候很有可能和已有的委托撮合完成了
        // 如果是这个情况没有必要生成order对象 放入orderBucket


        // 也就是先找到这个order最匹配的orderBucket
        // 先判断委托订单的方向
        // 如果订单是卖单，那么需要找买单对应的orderBucket,反之亦然
        // 接下来找价格最合适的单子
        // eg: 当前的 委托是卖单 单子是 50元 100股 ，那么就需要去买单的orderBucket中 找 所有 >=50的order
        // eg: 当前的 委托是买单 单子是 40元 200股 ，那么就需要去卖单的orderBucket中 找 所有 <=40的order
        NavigableMap<Long, IOrderBucket> subMatchBuckets =
                (cmd.direction == OrderDirection.SELL ? buyBuckets : sellBuckets)
                        .headMap(cmd.price, true);
        // 返回的是预撮合之后成交的委托量
        Long tVolume = preMatch(cmd, subMatchBuckets);
        // 如果与撮合的成交量 == 当前委托的委托量
        if (tVolume == cmd.volume) {
            // 说明 不需要生成真正的委托
            return CmdResultCode.SUCCESS;
        }
        final Order order = Order.builder()
                .mid(cmd.mid)
                .uid(cmd.uid)
                .code(cmd.code)
                .direction(cmd.direction)
                .price(cmd.price)
                .volume(cmd.volume)
                .tvolume(tVolume)
                .oid(cmd.oid)
                .timestamp(cmd.timestamp)
                .build();
        // 如果经过预撮合 没有任何成交 那么需要发送 成功接收委托的事件
        if (tVolume == 0) {
            genMatchEvent(cmd, OrderStatus.ORDER_ED);
        } else {// 如果经过预撮合 成交了一部分 那么需要发送 已经部分成交的事件
            genMatchEvent(cmd, OrderStatus.PART_TRADE);
        }

        // 放入orderBucket
        // 根据委托方向 找到 存放orderBucket的集合
        final IOrderBucket bucket = (cmd.direction == OrderDirection.SELL ? sellBuckets : buyBuckets)
                // 根据委托的价格 判断 存放orderBucket的集合 中 是否存在委托订单的价格对应的orderBucket
                .computeIfAbsent(cmd.price,
                        // 如果没有，根据当前委托订单的价格生成新的orderBucket
                        p -> {
                            final IOrderBucket b = IOrderBucket.create(IOrderBucket.OrderBucketImplType.GUDY);
                            b.setPrice(p);
                            return b;
                        });
        // 把委托放入orderBucket
        bucket.put(order);
        // 委托放入 委托的缓存
        oidMap.put(cmd.oid, order);


        return null;
    }

    /**
     * 预撮合
     *
     * @param cmd             当前委托
     * @param matchingBuckets 为了预撮合 根据当前委托 找到的对应的orderBucket
     * @Return 返回的是预撮合之后成交的委托量
     */
    private long preMatch(RingBufferCmd cmd, NavigableMap<Long, IOrderBucket> matchingBuckets) {
        //预撮合的成交量
        int tVolume = 0;
        // 说明 订单 没有 对应的能预撮合的orderBucket
        // 也就是说 但订单 没有与之对应的(卖单/卖单) 匹配
        if (matchingBuckets.size() == 0) {
            return tVolume;//预撮合的成交量 = 0
        }
        // 专门存储 某一给价格的orderBucket用尽了的情况
        List<Long> emptyBuckets = Lists.newArrayList();
        // 因为 IOrderBucket 实现了comparable接口 所以一定按照制定顺序排列
        // 如果是RingBufferCmd cmd 是买委托  matchingBuckets 一定是按照价格从低到高排列
        // 如果是RingBufferCmd cmd 是卖委托  matchingBuckets 一定是按照价格从高到低排列
        for (IOrderBucket bucket : matchingBuckets.values()) {
            // 遍历OrderBucket 本质就是 对orderBucket中每一个order委托订单的撮合
            // 撮合的数量 就是 cmd.volume -  tVolume 预撮合订单的剩余委托量
            tVolume += bucket.match(cmd.volume - tVolume, cmd,
                    order -> oidMap.remove(order.getOid()));
            // 这一轮撮合 导致 orderBucket的待成交量 == 0  说排名 这个orderBucket没有用了 删去
            if (bucket.getTotalVolume() == 0) {
                emptyBuckets.add(bucket.getPrice());
            }
            // TODO 不太懂
            // 已经预撮合的成交量 = 需要被撮合的全部数量
            if (tVolume == cmd.volume) {
                break;
            }
        }
        // 真正移除
        emptyBuckets.forEach(matchingBuckets::remove);

        return tVolume;
    }

    @Override
    public CmdResultCode cancelOrder(RingBufferCmd cmd) {
        // 从缓存中 移除委托
        Order order = oidMap.get(cmd.oid);
        if (order == null) {
            return CmdResultCode.INVALID_ORDER_ID;
        }
        oidMap.remove(order.getOid());
        // 从orderBucket中 撤销委托
        // 1.根据买卖方向找到对应的map
        NavigableMap<Long, IOrderBucket> bucketNavigableMap = order.getDirection() == OrderDirection.SELL ? sellBuckets : buyBuckets;
        // 2.根据委托的价格找到orderBucket
        IOrderBucket orderBucket = bucketNavigableMap.get(order.getPrice());
        // 3.从对饮的个orderBucket中 移除真正的委托
        orderBucket.remove(order.getOid());
        // 如果 被移除的份额委托 是orderBucket中最后一笔委托
        // 判断方式就是 orderBucket剩余的委托量 == 0 也就是没有委托
        if (orderBucket.getTotalVolume() == 0) {
            // 对应的orderBucket需要在存放orderBucket的集合中被移除
            bucketNavigableMap.remove(order.getPrice());
        }

        // 告诉下游撤单成功 也就是发送事件
        MatchEvent cancelEvent = new MatchEvent();
        cancelEvent.timestamp = System.currentTimeMillis();
        cancelEvent.mid = order.getMid();
        cancelEvent.oid = order.getOid();
        /**
         * 判断这个委托单子 的成交量
         * 成交量 = 0  全部撤销
         * 成交量 > 0  未成交的撤销
         */
        cancelEvent.status = order.getTvolume() == 0 ? OrderStatus.CANCEL_ED : OrderStatus.PART_CANCEL;
        cancelEvent.volume = order.getTvolume() - order.getVolume();
        cmd.matchEventList.add(cancelEvent);

        return CmdResultCode.SUCCESS;
    }

    @Override
    public void fillCode(L1MarketData data) {
        data.code = code;
    }

    /**
     * 档位   sellprices  sellVolumes
     * 卖五   46.93        5
     * 卖四   46.92        10
     * 卖三   46.91        296
     * 卖二   46.90        35
     * 卖一   46.88        601
     *
     * @param size
     * @param data
     */
    @Override
    public void fillSells(int size, L1MarketData data) {
        if (size == 0) {// 要求填写的size是0 说明 不需要去真实的orderBucket中找数据
            data.sellSize = 0;
            return;
        }
        // 遍历整个卖的bucket集合
        int i = 0;
        for (IOrderBucket bucket : sellBuckets.values()) {
            data.sellPrices[i] = bucket.getPrice();
            data.sellVolumes[i] = bucket.getTotalVolume();
            if (++i == size) {
                break;
            }
        }

        data.sellSize = i;
    }

    /**
     * 档位   buyprices  buyVolumes
     * 买一   46.87        30
     * 买二   46.86        51
     * 买三   46.85        10
     * 买四   46.83        16
     * 买五   46.82        52
     *
     * @param size
     * @param data
     */
    @Override
    public void fillBuys(int size, L1MarketData data) {
        if (size == 0) {
            data.buySize = 0;
            return;
        }
        // 遍历整个买的bucket集合
        int i = 0;
        for (IOrderBucket bucket : buyBuckets.values()) {
            data.buyPrices[i] = bucket.getPrice();
            data.buyVolumes[i] = bucket.getTotalVolume();
            if (++i == size) {
                break;
            }
        }

        data.buySize = i;
    }

    @Override
    public int limitBuyBucketSize(int maxSize) {
        // 理论上maxSize(==5) 和 实际的market行情的档位 取最小值
        return Math.min(maxSize, buyBuckets.size());
    }

    @Override
    public int limitSellBucketSize(int maxSize) {
        // 理论上maxSize(==5) 和 实际的market行情的档位 取最小值
        return Math.min(maxSize, sellBuckets.size());
    }


    /**
     * 生成matchevent
     *
     * @param cmd
     * @param status
     */
    private void genMatchEvent(RingBufferCmd cmd, OrderStatus status) {
        long now = System.currentTimeMillis();
        MatchEvent event = new MatchEvent();
        event.timestamp = now;// 时间戳
        event.mid = cmd.mid;// 会员号
        event.oid = cmd.oid;// 委托编号
        event.status = status;// 状态
        event.volume = 0;// 量
        cmd.matchEventList.add(event);
    }
}
