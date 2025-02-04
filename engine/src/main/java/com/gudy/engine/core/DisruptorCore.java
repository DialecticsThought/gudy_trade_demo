package com.gudy.engine.core;

import com.gudy.engine.bean.RingBufferCmdFactory;
import com.gudy.engine.bean.command.RingBufferCmd;
import com.gudy.engine.bean.handler.BaseHandler;
import com.gudy.engine.bean.handler.DisruptorExceptionHandler;
import com.gudy.engine.bean.task.HqPubTask;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import org.springframework.stereotype.Component;
import java.util.Timer;


import static com.gudy.engine.bean.handler.L1PubHandler.HQ_PUB_RATE;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/11 13:54
 */
@Log4j2
@Data
@Component
public class DisruptorCore {
    private  Disruptor<RingBufferCmd> disruptor;

    private static final int RING_BUFFER_SIZE = 1024;

    public DisruptorCore() {
        this.disruptor = new Disruptor<>(
                new RingBufferCmdFactory(),// 线程的工厂类

                RING_BUFFER_SIZE,// 缓存大小

                new AffinityThreadFactory("aft_engine_core", AffinityStrategies.ANY),// 线程产生的工厂

                ProducerType.SINGLE,// 生产线程的类型

                new BlockingWaitStrategy()// 消费线程的等待策略
        );
        //this.engineCore = new EngineCore(disruptor.getRingBuffer());
    }

    /**
     * 绑定消费者 + 启动 disruptor
     *
     * @param riskHandler
     * @param matchHandler
     * @param pubHandler
     */
    public void bindingHandlerAndStart(BaseHandler riskHandler,
                                       BaseHandler matchHandler,
                                       BaseHandler pubHandler) {
        //1.全局异常处理器
        final DisruptorExceptionHandler<RingBufferCmd> exceptionHandler = new DisruptorExceptionHandler<>(
                "main",
                (ex, seq) -> {
                    log.error("exception thrown on seq={}", seq, ex);
                });
        disruptor.setDefaultExceptionHandler(exceptionHandler);
        //2. 前置风控处理器(消费者) --> 撮合处理器(消费者) --> 发布数据处理器(消费者)
        disruptor.handleEventsWith(riskHandler)
                .then(matchHandler)
                .then(pubHandler);
        //3.启动
        disruptor.start();
        log.info("match engine start");
        //4.定时发布行情任务(其中一个生产者)
        startScheduler();
    }

    /**
     * 生产者1: 本质是一个定时任务, 通知disruptor 也就是撮合核心要对外发布行情的定时任务
     * 生产者2: 从排队机过来的数据(广播 也就是CmdPacketQueue的init方法)
     * 定时发布行情任务(其中一个生产者)
     */
    public void startScheduler() {
        new Timer().schedule(new HqPubTask(), 5000, HQ_PUB_RATE);
    }
}
