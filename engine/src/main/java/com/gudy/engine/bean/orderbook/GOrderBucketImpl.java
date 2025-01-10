package com.gudy.engine.bean.orderbook;


import com.gudy.engine.bean.command.RingBufferCmd;
import com.gudy.engine.thirdpart.order.OrderStatus;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
@ToString
@Data
public class GOrderBucketImpl implements IOrderBucket {
    /**
     * 1.当前 orderBucket对象的价格
     */
    private long price;

    /**
     * 2.当前 orderBucket对象的委托量
     */
    private long totalVolume = 0;

    /**
     * 3.委托列表
     * 要求
     * 很快的把新的委托加入
     * 很快的根据id吧对应的order排除
     */
    private final LinkedHashMap<Long, Order> entries = new LinkedHashMap<>();


    @Override
    public void put(Order order) {
        entries.put(order.getOid(), order);
        // 新总量 = 原总量 + 新委托的总量 - 新委托的已成交量
        // 新增的委托单已成交部分实际上不应计入桶内的挂单总量，因为它已经被撮合出去，与对手订单成交了
        totalVolume += order.getVolume() - order.getTvolume();
    }

    /**
     * @param oid
     * @return
     */
    @Override
    public Order remove(Long oid) {
        //防止重复执行删除订单的请求
        Order order = entries.get(oid);
        // 实际情况 用户可能撤单过了，但是因为网络没有显示，用户点了多次撤单，所以需要判断是否已经没有单子了
        if (order == null) {
            return null;
        }
        entries.remove(oid);

        totalVolume -= order.getVolume() - order.getTvolume();

        // 返回上层 被撤单的委托
        return order;
    }

    /**
     * @param volumeLeft          剩余需要撮合的委托量  也就是有多少量 要在orderBucket中处理掉
     * @param triggerCmd          过来的委托
     * @param removeOrderCallback 回调函数 处理完成之后 在orderbook类中的操作
     * @return
     */
    @Override
    public long match(Long volumeLeft, RingBufferCmd triggerCmd, Consumer<Order> removeOrderCallback) {
        /**
         * eg:
         * 有两档 卖单
         * 价格 46 --> 有这3笔委托 并且顺序是 5 10 24
         * 价格 45 --> 有这4笔委托 并且顺序是 11 20 10 20
         * 现在出现了一笔买单
         * 45 100
         * 他回去所有卖单对应的bucket里面，找对这笔买单而言最有优势的那一档（所谓的优势就是以最低的价格买到）
         * 那么就是说会在 45这挡，然后根据顺序11 20 10 20 依次抵消
         * 100 - 11 - 20 - 10 - 20 = 39 这留下的变成新的委托挂在orderBucket (TODO这里不考虑 剩下的撤单的情况)
         */
        Iterator<Map.Entry<Long, Order>> iterator = entries.entrySet().iterator();
        // 这个变量 就是记录 这个委托 在整个orderBucket中 能吃掉多少量
        long volumeMatch = 0;

        // 订单簿中订单未遍历完且当前触发订单仍有剩余量未成交时，继续撮合
        while (iterator.hasNext() && volumeLeft > 0) {// volumeLeft = 0 就说明不需要撮合了
            // 获取当前价格档中的一个订单（FIFO 方式）
            Map.Entry<Long, Order> next = iterator.next();
            Order order = next.getValue();
            // 触发订单的剩余量
            // order.getVolume() - order.getTvolume()：当前订单的 剩余可成交量（总量减去已成交量）
            // 两者取最小值，表示本次撮合能成交的量
            long traded = Math.min(volumeLeft, order.getVolume() - order.getTvolume());
            // 累计这次撮合总共成交的量
            volumeMatch += traded;
            // 订单已成交量 order.getTvolume：当前订单的已成交量增加
            order.setTvolume(order.getTvolume() + traded);
            // 触发订单的剩余量 volumeLeft：减少本次撮合成交的量
            volumeLeft -= traded;
            // 触发订单的剩余量 volumeLeft：减少本次撮合成交的量
            totalVolume -= traded;
            // 检查当前订单是否 完全成交（已成交量等于总量）
            boolean fullMatch = order.getVolume() == order.getTvolume();
            // 调用 genMatchEvent 方法生成撮合事件，记录此次撮合的细节
            genMatchEvent(order, triggerCmd, fullMatch, volumeLeft == 0, traded);

            if (fullMatch) {// 如果订单完全成交
                // 执行回调函数，从订单系统中移除该订单
                removeOrderCallback.accept(order);
                // 从当前遍历中移除该订单
                iterator.remove();
            }
        }
        // 返回本次撮合的总成交量
        return volumeMatch;
    }

    /**
     * genMatchEvent 方法负责生成每次撮合的事件记录，包括 买单事件 和 卖单事件
     * 为什么需要生成两种事件？
     * 买方事件：记录触发订单（买单）的成交情况。
     * 卖方事件：记录当前撮合对手订单（卖单）的成交情况
     * 两个事件用于：
     * <p>
     * 追踪订单的成交过程；
     * 后续生成交易明细、统计数据
     * <p>
     * TODO 如果和多个卖方 或者买方 成交 会有多个事件
     *
     * @param order
     * @param cmd
     * @param fullMatch
     * @param cmdFullMatch
     * @param traded
     */
    private void genMatchEvent(final Order order, final RingBufferCmd cmd, boolean fullMatch, boolean cmdFullMatch, long traded) {
        // 时间戳 now：记录事件发生的时间
        long now = System.currentTimeMillis();
        // 交易 ID tid：系统为这次撮合生成的唯一标识符，用于追踪这笔交易
        long tid = IOrderBucket.tidGen.getAndIncrement();

        //两个MatchEvent

        // TODO 一次成交 有唯一的id 卖方 买方 的事件 靠这个id关联

        // 构造买方事件
        MatchEvent bidEvent = new MatchEvent();
        bidEvent.timestamp = now;
        bidEvent.mid = cmd.mid;// 会员号
        bidEvent.oid = cmd.oid;// 委托编号
        // 判断 是部分成交还是全部成交
        bidEvent.status = cmdFullMatch ? OrderStatus.TRADE_ED : OrderStatus.PART_TRADE;// 委托状态
        bidEvent.tid = tid;// 成交编号
        bidEvent.volume = traded;// 成交的量
        bidEvent.price = order.getPrice();// 成交的价格
        cmd.matchEventList.add(bidEvent);

        // 构造卖方事件
        MatchEvent ofrEvent = new MatchEvent();
        ofrEvent.timestamp = now;
        ofrEvent.mid = order.getMid();
        ;// 会员号
        ofrEvent.oid = order.getOid();// 委托编号
        ofrEvent.status = fullMatch ? OrderStatus.TRADE_ED : OrderStatus.PART_TRADE;// 委托状态
        ofrEvent.tid = tid;// 成交编号
        ofrEvent.volume = traded;// 成交的量
        ofrEvent.price = order.getPrice();// 成交的价格
        cmd.matchEventList.add(ofrEvent);

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GOrderBucketImpl that = (GOrderBucketImpl) o;

        return new EqualsBuilder()
                .append(price, that.price)
                .append(entries, that.entries)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(price)
                .append(entries)
                .toHashCode();
    }
}
