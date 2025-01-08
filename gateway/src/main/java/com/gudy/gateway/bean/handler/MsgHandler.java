package com.gudy.gateway.bean.handler;

import com.gudy.gateway.thirdpart.bean.CommonMsg;
import io.vertx.core.net.NetSocket;

/**
 * @Description 消息处理类
 * @Author veritas
 * @Data 2025/1/7 17:35
 */
public interface MsgHandler {

    /*
     * 连接的消息 不是重点 默认实现
     * */
    default void onConnect(NetSocket socket) {
    }

    ;

    /*
     * 断开的消息 不是重点 默认实现
     * */
    default void onDisconnect(NetSocket socket) {
    }

    ;

    /*
     * 处理异常的消息 不是重点 默认实现
     * */
    default void onException(NetSocket socket, Throwable cause) {
    }

    ;

    /*
     * 处理柜台的消息
     * */
    void onCounterData(CommonMsg msg);
}
