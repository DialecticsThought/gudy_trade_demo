package com.gudy.engine.bean.task;

import com.gudy.engine.core.EngineCore;
import com.gudy.engine.thirdpart.order.CmdType;
import com.gudy.engine.thirdpart.order.OrderCmd;
import jakarta.annotation.Resource;

import java.util.TimerTask;

/**
 * @Description TODO 发布行情的定时任务
 * 生产者1: 本质是一个定时任务, 通知disruptor 也就是撮合核心要对外发布行情的定时任务
 * 生产者2: 从排队机过来的数据(广播 也就是CmdPacketQueue的init方法)
 * 定时发布行情任务(其中一个生产者)
 * @Author veritas
 * @Data 2025/1/11 14:20
 */
public class HqPubTask extends TimerTask {
    @Resource
    private EngineCore engineCore;

    @Override
    public void run() {
        // 发布行情本质是一个指令 没有实际的数据
        engineCore.submitCommand(
                OrderCmd.builder()
                        .type(CmdType.HQ_PUB)
                        .build()
        );
    }
}
