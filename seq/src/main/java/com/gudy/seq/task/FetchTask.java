package com.gudy.seq.task;


import com.alipay.sofa.jraft.util.Bits;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.google.common.collect.Lists;
import com.gudy.seq.Bean.SequenceCore;
import com.gudy.seq.thirdpart.bean.CmdPack;
import com.gudy.seq.thirdpart.fetchsurv.IFetchService;
import com.gudy.seq.thirdpart.order.OrderCmd;
import com.gudy.seq.thirdpart.order.OrderDirection;
import io.vertx.core.buffer.Buffer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;


import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/8 19:24
 */

@Log4j2
@RequiredArgsConstructor
public class FetchTask extends TimerTask {

    @NonNull
    private SequenceCore sequenceCore;

    /*
     * 从map中 得到所有网关的连接
     * 遍历连接
     * 捞取数据
     * 定序
     * */
    @Override
    public void run() {
        /**
         * 因为 排队机是raft集群 只要和集群的leader 交互
         */
        if (!sequenceCore.getNode().isLeader()) {
            return;
        }
        // 遍历网关
        Map<String, IFetchService> fetchServiceMap = sequenceCore.getFetchServiceMap();
        if (MapUtils.isEmpty(fetchServiceMap)) {
            return;
        }
        //获取数据
        List<OrderCmd> cmds = collectAllOrders(fetchServiceMap);
        if (CollectionUtils.isEmpty(cmds)) {
            return;
        }
        log.info(cmds);
        //对数据进行排序
        // 排序 时间优先 价格优先 量优先
        cmds.sort((o1, o2) -> {
            //第一种写法
            //时间优先 价格优先 量优先
            //第二种写法
            int res = compareTime(o1, o2);
            if (res != 0) {
                return res;
            }

            res = comparePrice(o1, o2);
            if (res != 0) {
                return res;
            }

            res = compareVolume(o1, o2);
            return res;
        });

        // 存储到KV Store + 发送到撮合核心
        try {
            // 1.生成Packetno TODO 包的序号是用来判断是否乱序 是否丢包的依据
            long packetNo = getPacketNoFromStore();
            // 2.入库 报的序列号  对应的委托数据
            CmdPack pack = new CmdPack(packetNo, cmds);
            // 序列化 对象
            byte[] serialize = sequenceCore.getCodec().serialize(pack);
            // 入库
            insertToKvStore(packetNo, serialize);
            // 3.更新packetno+1
            updatePacketNoInStore(packetNo + 1);
            //4.发送
            sequenceCore.getMulticastSender().send(
                    Buffer.buffer(serialize),
                    sequenceCore.getSequenceConfig().getMulticastPort(),
                    sequenceCore.getSequenceConfig().getMulticastIp(),
                    null // 这个处理器是数据包离开本机网卡之后执行的 这里没有需求
            );
        } catch (Exception e) {
            log.error("encode cmd packet error", e);
        }
    }

    /**
     * 更新PacketNo 包的序号
     *
     * @param packetNo
     */
    private void updatePacketNoInStore(long packetNo) {
        // 在 kv 数据库中 key是字节数组 所以定义一个字节数组 把 packetNo放入字节数组
        final byte[] bytes = new byte[8];
        Bits.putLong(bytes, 0, packetNo);
        // put是异步操作 bput是同步操作
        sequenceCore.getNode().getRheaKVStore().put(PACKET_NO_KEY, bytes);
    }

    /**
     * 保存数据到KV Store
     *
     * @param packetNo
     * @param serialize
     */
    private void insertToKvStore(long packetNo, byte[] serialize) {
        // 在 kv 数据库中 key是字节数组 所以定义一个字节数组 把 packetNo放入字节数组
        byte[] key = new byte[8];
        Bits.putLong(key, 0, packetNo);
        // put是异步操作 bput是同步操作
        sequenceCore.getNode().getRheaKVStore().put(key, serialize);
    }

    /**
     * k v store 里面所有的key是用byte表示
     * 指定 seq_pqcket_no 这个key对应的value存的是packet_no
     * */
    private static final byte[] PACKET_NO_KEY = BytesUtil.writeUtf8("seq_pqcket_no");

    /**
     * 获取PacketNo
     *
     * @return
     */
    private long getPacketNoFromStore() {
        final byte[] bPacketNo = sequenceCore.getNode().getRheaKVStore().bGet(PACKET_NO_KEY);
        // 初始化 包的序号
        long packetNo = 0;
        // 刚开始 kv 可以为空
        if (ArrayUtils.isNotEmpty(bPacketNo)) {
            packetNo = Bits.getLong(bPacketNo, 0);
        }
        return packetNo;
    }

    private int compareVolume(OrderCmd o1, OrderCmd o2) {
        if (o1.volume > o2.volume) {
            return -1;
        } else if (o1.volume < o2.volume) {
            return 1;
        } else {
            return 0;
        }
    }

    private int comparePrice(OrderCmd o1, OrderCmd o2) {
        if (o1.direction == o2.direction) {
            if (o1.price > o2.price) {
                return o1.direction == OrderDirection.BUY ? -1 : 1;
            } else if (o1.price < o2.price) {
                return o1.direction == OrderDirection.BUY ? 1 : -1;
            } else {
                return 0;
            }
        }
        return 0;
    }

    private int compareTime(OrderCmd o1, OrderCmd o2) {
        if (o1.timestamp > o2.timestamp) {
            return 1;
        } else if (o1.timestamp < o2.timestamp) {
            return -1;
        } else {
            return 0;
        }
    }

    private List<OrderCmd> collectAllOrders(Map<String, IFetchService> fetchServiceMap) {
        //推荐~~
        List<OrderCmd> msgs = Lists.newArrayList();
        fetchServiceMap.values().forEach(t -> {
            List<OrderCmd> orderCmds = t.fetchData();
            if (CollectionUtils.isNotEmpty(orderCmds)) {
                msgs.addAll(orderCmds);
            }
        });
        return msgs;
    }
}
