package com.gudy.engine.bean;

import com.google.common.collect.Lists;
import com.gudy.engine.bean.command.CmdResultCode;
import com.gudy.engine.bean.command.RingBufferCmd;
import com.lmax.disruptor.EventFactory;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

public class RingBufferCmdFactory implements EventFactory<RingBufferCmd> {
    @Override
    public RingBufferCmd newInstance() {
        return RingBufferCmd.builder()
                .resultCode(CmdResultCode.SUCCESS)
                .matchEventList(Lists.newArrayList())
                .marketDataMap(new IntObjectHashMap<>())
                .build();
    }
}
