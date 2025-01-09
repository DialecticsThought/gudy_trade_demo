package com.gudy.engine.bean;

import com.lmax.disruptor.EventFactory;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/9 11:24
 */
public class RbCmdFactory implements EventFactory<RbCmd> {
    @Override
    public RbCmd newInstance() {
        return RbCmd.builder().code(0).msg("").build();
    }
}
