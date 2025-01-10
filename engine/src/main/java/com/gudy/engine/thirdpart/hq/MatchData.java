package com.gudy.engine.thirdpart.hq;

import com.gudy.engine.thirdpart.order.OrderStatus;
import lombok.Builder;


import java.io.Serializable;

/**
 *
 * 这个类是专门放入总线 给柜台和其他模块用的类
 */
@Builder
public class MatchData implements Serializable {
    /**
     *
     */
    public long timestamp;

    public short mid;

    public long oid;

    public OrderStatus status;

    public long tid;

    //撤单数量 成交数量
    public long volume;

    public long price;

}
