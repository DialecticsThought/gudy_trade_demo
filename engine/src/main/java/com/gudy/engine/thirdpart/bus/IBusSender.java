package com.gudy.engine.thirdpart.bus;


import com.gudy.engine.thirdpart.bean.CommonMsg;

public interface IBusSender {
    /**
     * 启动总线
     */
    void startup();

    /**
     * 往总线发送消息
     *
     * @param commonMsg 所有在总线上传输的数据都是这个类
     */
    void publish(CommonMsg commonMsg);

}
