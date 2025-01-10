package com.gudy.engine.bean.command;

import com.gudy.engine.bean.orderbook.MatchEvent;
import com.gudy.engine.thirdpart.hq.L1MarketData;
import com.gudy.engine.thirdpart.order.CmdType;
import com.gudy.engine.thirdpart.order.OrderDirection;
import com.gudy.engine.thirdpart.order.OrderType;
import lombok.Builder;
import lombok.ToString;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.List;

@Builder
@ToString
public class RingBufferCmd {
    /**
     * 产生rbCmd的时间错
     */
    public long timestamp;
    /**
     * 会员id
     */
    public short mid;
    /**
     * 用户id
     */
    public long uid;
    /**
     * orderCmd类型
     */
    public CmdType command;
    /**
     * 股票代码
     */
    public int code;

    public OrderDirection direction;
    /**
     * 价格
     */
    public long price;
    /**
     * 量
     */
    public long volume;
    /**
     * 委托编号
     */
    public long oid;
    /**
     * 委托类型
     */
    public OrderType orderType;

    /**
     * 保存撮合结果
     * MatchEvent是撮合模块发布撮合结果的成员变量
     * 所有的撮合结果会放入该链表中，流转到对应的handler进行处理
     */
    public List<MatchEvent> matchEventList;

    /**
     * 前置风控 --> 撮合 --> 发布
     * 因为3种消费线程
     * 1.前置风控
     * 2.撮合
     * 3.发布
     * 这个变量是 3种线程的通信媒介
     */
    public CmdResultCode resultCode;

    /**
     * 保存行情
     * L1MarketData是保存行情的成员变量
     * 所有的行情会放入该链表中，流转到对应的handler进行处理
     */
    public IntObjectHashMap<L1MarketData> marketDataMap;

}
