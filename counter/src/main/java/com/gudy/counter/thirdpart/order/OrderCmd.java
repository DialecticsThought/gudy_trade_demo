package com.gudy.counter.thirdpart.order;

import lombok.Builder;
import lombok.ToString;

import java.io.Serializable;

/*
* TODO
*  这个 bean 在 委托终端 与 柜台之间
*  也在 柜台 与 报盘机 之间
*
* */
@Builder
@ToString
public class OrderCmd implements Serializable {
    /**
     * 指令类型
     */
    public CmdType type;
    /*
    * 时间戳
    * */
    public long timestamp;

    /**
     * 会员ID  柜台自带的
     * 有几家交易所是会员制，这个id就标志了这几家交易所的会员的id
     * 该id涉及到盘后数据的清算
     * 有了这个字段 交易撮合核心就知道 这个委托是哪一个券商或者机构发送过来的
     */
    final public long mid;

    /**
     * 用户ID
     */
    final public long uid;

    /**
     * 股票代码
     */
    final public int code;

    /**
     * 委托方向
     */
    final public OrderDirection direction;

    /**
     * 委托价格
     */
    final public long price;

    /**
     * 委托量
     */
    final public long volume;

    /**
     * 委托类型
     * 1.LIMIT
     */
    final public OrderType orderType;

    /**
     * 委托编号
     */
    public long oid;


}
