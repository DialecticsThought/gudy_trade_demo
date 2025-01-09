package com.gudy.engine.bean;

import com.alipay.remoting.exception.CodecException;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.util.Bits;
import com.google.common.collect.Lists;
import com.gudy.engine.core.EngineCore;
import com.gudy.engine.thirdpart.bean.CmdPack;
import com.gudy.engine.thirdpart.order.OrderCmd;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO 每一个撮合核心 只有一个缓存队列 可以用单例
 * @Author veritas
 * @Data 2025/1/9 15:04
 */
@Component
@Log4j2
@Data
public class CmdPacketQueue {

    private BlockingQueue<CmdPack> recvCache = new LinkedBlockingQueue<>();
    @Resource
    private EngineCore engineCore;
    // 初始化 上一个数据包的序号的变量 如果还没有接受过任何一个数据包 就是-1
    private static volatile long lastPackNo = -1;

    public void cache(CmdPack pack) {
        recvCache.offer(pack);
    }

    public void init() {
        /*
         * 该线程中 不断轮询缓存 并拿取数据
         * */
        new Thread(new HandleCmdPackThread(this, this.engineCore, lastPackNo)).start();
    }
}

@Log4j2
@NoArgsConstructor
class HandleCmdPackThread implements Runnable {

    private CmdPacketQueue cmdPacketQueue;
    // 外部传入
    private long lastPackNo;
    // 外部传入
    private EngineCore engineCore;

    public HandleCmdPackThread(CmdPacketQueue cmdPacketQueue, EngineCore engineCore, long lastPackNo) {
        this.cmdPacketQueue = cmdPacketQueue;
        this.engineCore = engineCore;
        this.lastPackNo = lastPackNo;
    }

    @Override
    public void run() {
        BlockingQueue<CmdPack> recvCache = cmdPacketQueue.getRecvCache();
        while (true) {
            try {
                // 得到数据
                // EngineCore 在监听udp组播的时候得到的数据
                CmdPack cmds = recvCache.poll(10, TimeUnit.SECONDS);

            } catch (Exception e) {
                log.error("msg packet recvCache error, continue", e);
            }
        }
    }

    public void handle(CmdPack cmdPack) {
        log.info("recv: {}", cmdPack);
        // 校验得到的数据包的序号
        long packNo = cmdPack.getPackNo();
        if (packNo == lastPackNo + 1) {// 正常情况 是顺序的
            if (CollectionUtils.isEmpty(cmdPack.getOrderCmds())) {
                return;
            }
            for (OrderCmd cmd : cmdPack.getOrderCmds()) {
                engineCore.submitCommand(cmd);
            }
        } else if (packNo <= lastPackNo) {// 说明收到历史的数据包
            // 避免重复收集数据包
            log.warn("recv duplicat packId: {}", packNo);
        } else {// 不连续 eg: 上一次收到3号 这一次收到6号 中间的数据包 不在
            log.info("packNo lost from {} to {} ,begin query from sequencer", lastPackNo + 1, packNo);
            // TODO 主动的向排队机要数据
            // 请求数据 我要 区间[lastPackNo + 1,packNo + 1]的数据
            byte[] firstKey = new byte[8];
            Bits.putLong(firstKey, 0, lastPackNo + 1);

            byte[] lastKey = new byte[8];
            Bits.putLong(lastKey, 0, packNo + 1);
            // 去kv数据 那这段数据
            List<KVEntry> kvEntries = engineCore.getOrderKVStore().bScan(firstKey, lastKey);
            //判断排队机的kv数据中是否存在这些数据
            if (!CollectionUtils.isEmpty(kvEntries)) {
                List<CmdPack> collect = Lists.newArrayList();
                // 遍历数据 转换数据
                for (KVEntry entry : kvEntries) {
                    byte[] value = entry.getValue();
                    if (ArrayUtils.isNotEmpty(value)) {
                        try {
                            collect.add(engineCore.getBodyCodec().deserialize(value, CmdPack.class));
                        } catch (CodecException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                // 排序
                collect.sort((o1, o2) -> (int) (o1.getPackNo() - o2.getPackNo()));

                for (CmdPack pack : collect) {
                    // 严谨 再判断一次
                    if (CollectionUtils.isEmpty(pack.getOrderCmds())) {
                        continue;
                    }
                    for (OrderCmd cmd : pack.getOrderCmds()) {
                        engineCore.submitCommand(cmd);
                    }
                }
            }
            // 排队机出现问题 导致了 跳号
            lastPackNo = packNo;
            log.error("no kv entries found");
        }
    }
}
