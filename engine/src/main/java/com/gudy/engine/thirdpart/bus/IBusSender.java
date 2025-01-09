package com.gudy.engine.thirdpart.bus;


import com.gudy.engine.thirdpart.bean.CommonMsg;

public interface IBusSender {

    void startup();

    void publish(CommonMsg commonMsg);

}
