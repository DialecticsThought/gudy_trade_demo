package com.gudy.engine.bean.orderbook;

import com.gudy.engine.bean.command.RingBufferCmd;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/10 14:03
 */
public interface IOrderBucket extends Comparable<IOrderBucket> {

    AtomicLong tidGen = new AtomicLong(0);

    /**
     * 新增订单
     */
    void put(Order order);

    /**
     * 移除订单
     * 不光是撤单，如果某一卖单，把一批买单都吃掉的话，被吃掉的买单 会从orderBucket移除
     * 根据订单号移除
     *
     * @param oid
     * @return
     */
    Order remove(Long oid);

    /**
     * 匹配订单功能
     *
     * @param volumeLetf          剩余需要撮合的委托量  也就是有多少量 要在orderBucket中处理掉
     * @param trrigerCmd          过来的委托
     * @param removeOrderCallback 回调函数 处理完成之后 在orderbook类中的操作
     * @return
     */
    long match(Long volumeLetf, RingBufferCmd trrigerCmd, Consumer<Order> removeOrderCallback);

    //行情发布

    /**
     * 要知道orderBucket绑定的价格
     *
     * @return
     */
    Long getPrice();

    /**
     * 设置orderBucket对应的价格
     * @param price
     */
    void setPrice(long price);

    /**
     * 得到OrderBucket对应总量
     *
     * @return
     */
    Long getTotalVolume();


    /**
     * 5.初始化orderBucket
     * @param type
     * @return
     */
    static IOrderBucket create(OrderBucketImplType type) {
        switch (type) {
            /**
             * 每一个case 就是orderBucket的实现类
             */
            case GUDY:
                return new GOrderBucketImpl();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Getter
    enum OrderBucketImplType {
        GUDY(0);

        private byte code;

        OrderBucketImplType(int code) {
            this.code = (byte) code;
        }
    }


    //6.比较 排序
    default int compareTo(IOrderBucket other) {
        return Long.compare(this.getPrice(), other.getPrice());
    }
}
