package com.gudy.seq.thirdpart.order;

import lombok.Getter;

@Getter
public enum OrderStatus {
//            正撤：撤单指令已送达公司，正在等待处理，此时不能确定是否已进场；
//            部撤：委托指令已成交一部分，未成交部分被撤销；
//            已撤：委托指令全部被撤消；

//            正报：委托指令已送达公司，正在等待处理，此时不能确定是否已进场；
//            已报：已收到下单反馈；

//            已成：委托指令全部成交；
//            部成：委托指令部份成交；

//            废单，表示撤单指令失败，原因可能是被撤的下单指令已经成交了或场内无法找到这条下单记录；

    NOT_SET(-1),

    CANCEL_STANDBY(0),
    CANCEL_ED(1),
    PART_CANCEL(2),

    ORDER_STANDBY(3),
    ORDER_ED(4),

    TRADE_ED(5),
    PART_TRADE(6),

    FAIL(7);


    private int code;

    OrderStatus(int code) {
        this.code = code;
    }


}
