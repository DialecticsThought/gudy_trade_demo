package com.gudy.engine.bean.handler;

import com.gudy.engine.bean.command.RingBufferCmd;
import com.lmax.disruptor.EventHandler;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/10 13:35
 */
public abstract class BaseHandler implements EventHandler<RingBufferCmd> {
}
