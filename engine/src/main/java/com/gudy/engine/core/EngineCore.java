package com.gudy.engine.core;

import com.alipay.remoting.exception.CodecException;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.gudy.engine.bean.CmdPacketQueue;
import com.gudy.engine.bean.command.CmdResultCode;
import com.gudy.engine.bean.command.RbCmd;
import com.gudy.engine.config.EngineConfig;
import com.gudy.engine.thirdpart.bean.CmdPack;
import com.gudy.engine.thirdpart.checksum.IChecksum;
import com.gudy.engine.thirdpart.codec.IBodyCodec;
import com.gudy.engine.thirdpart.codec.IMsgCodec;
import com.gudy.engine.thirdpart.order.CmdType;
import com.gudy.engine.thirdpart.order.OrderCmd;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;


/**
 * @Description 1.流转到 前置风控
 * 2.订流转到 订单簿 完成撮合
 * 3.订流转到 发布线程 根据撮合的结果进行航行的发布
 * @Author veritas
 * @Data 2025/1/9 14:48
 */
@Component
@Data
@Log4j2
public class EngineCore {

    private RingBuffer<RbCmd> ringBuffer;
    @Resource
    private EngineConfig engineConfig;
    @Resource
    private CmdPacketQueue cmdPacketQueue;

    private IBodyCodec bodyCodec;

    private IChecksum cs;

    private IMsgCodec msgCodec;

    private Vertx vertx = Vertx.vertx();

    private RheaKVStore orderKVStore = new DefaultRheaKVStore();

    @PostConstruct
    public void startup() {

        // 启动撮合核心
        startEngine();
        // 建立总线连接

        // 初始化排队机的kv store的链接
        startSeqConn();
    }

    public void startEngine() {
        // 上下游通信的模版 是orderCmd 不能直接放入disruptor队列 需要用RbCmd

        // 前置风控处理器
    }

    private void startSeqConn() {
        List<RegionRouteTableOptions> regionRouteTableOptions = MultiRegionRouteTableOptionsConfigured
                .newConfigured()
                .withInitialServerList(-1L, engineConfig.getSequenceList())
                .config();

        PlacementDriverOptions placementDriverOptions = PlacementDriverOptionsConfigured
                .newConfigured()
                .withFake(true)
                .withRegionRouteTableOptionsList(regionRouteTableOptions)
                .config();

        RheaKVStoreOptions rheaKVStoreOptions = RheaKVStoreOptionsConfigured
                .newConfigured()
                .withPlacementDriverOptions(placementDriverOptions)
                .config();

        orderKVStore.init(rheaKVStoreOptions);

        // 委托指令的队列
        cmdPacketQueue.init();

        // 启动udp 组播 组播 允许多个udp终端接受相同的数据包
        DatagramSocket datagramSocket = vertx.createDatagramSocket(new DatagramSocketOptions());

        datagramSocket.listen(engineConfig.getOrderRecvPort(), "0.0.0.0", asyncRes -> {
            if (asyncRes.succeeded()) {// 端口监听成功
                // 处理 得到的数据
                datagramSocket.handler(packet -> {
                    Buffer udpData = packet.data();

                    if (udpData.length() > 0) {// 说明有数据
                        try {
                            // 反序列化数据包
                            CmdPack deserializedCmdPack = bodyCodec.deserialize(udpData.getBytes(), CmdPack.class);
                            // 把 数据包 放入到缓存
                            cmdPacketQueue.cache(deserializedCmdPack);
                        } catch (CodecException e) {
                            log.error("decode packet error", e);
                        }
                    } else {
                        // 打印 数据包的发送者
                        log.error("recv empty udp packet from client:{} ", packet.sender().toString());
                    }
                });
                try {
                    /**
                     * 加入 组播 需要4个入参
                     * 需要监听的组播的ip
                     * 网卡的名字 通过名字 指定哪一个网卡去监听
                     * 发消息的原ip地址
                     * 异步处理器 这里就判断是否成功加入组播组
                     */
                    datagramSocket.listenMulticastGroup(
                            engineConfig.getOrderRecvIp(),
                            mainInterface().getName(),
                            null,
                            asyncRes2 -> {
                                log.info("listen succeed {}", asyncRes2.succeeded());
                            });
                } catch (Exception e) {
                    log.error(e);
                }
            } else { // 端口上监听失败
                log.error("listen failed", asyncRes.cause());
            }
        });
    }

    /**
     * 网卡需要满足
     * 1.有IPV4
     * 2.需要支持组播
     * 3.不能是虚拟网卡
     * 4.不能用loopback
     *
     * @return
     */
    private NetworkInterface mainInterface() throws SocketException {
        // 拿到所有的网卡
        ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        // 过滤
        NetworkInterface networkInterface = interfaces.stream().filter(t -> {
                    try {
                        boolean isLoopback = t.isLoopback();
                        boolean supportMulticast = t.supportsMulticast();
                        boolean isVirtualBox = t.getDisplayName().contains("VirtualBox")
                                || t.getDisplayName().contains("Host-only");
                        boolean hasIpV4 = t.getInterfaceAddresses().stream().anyMatch(interfaceAddress -> {
                            return interfaceAddress.getAddress() instanceof Inet4Address;
                        });
                        return !isLoopback && supportMulticast && !isVirtualBox && hasIpV4;
                    } catch (Exception e) {
                        log.error("find net interface error", e);
                    }
                    return false;
                }).sorted(Comparator.comparing(NetworkInterface::getName))// 根据名字排序  拿出第一个网卡即可
                .findFirst()
                .orElse(null);
        return networkInterface;
    }


    public void submitCommand(OrderCmd cmd) {
        switch (cmd.type) {
            case HQ_PUB:
                ringBuffer.publishEvent(HQ_PUB_TRANSLATOR, cmd);
                break;
            case NEW_ORDER:
                ringBuffer.publishEvent(NEW_ORDER_TRANSLATOR, cmd);
                break;
            case CANCEL_ORDER:
                ringBuffer.publishEvent(CANCEL_ORDER_TRANSLATOR, cmd);
                break;
            default:
                throw new IllegalArgumentException("Unsupported cmdType : " + cmd.getClass().getSimpleName());
        }
    }

    /**
     * 委托trans
     */
    private static final EventTranslatorOneArg<RbCmd, OrderCmd> NEW_ORDER_TRANSLATOR = (rbCmd, seq, newOrder) -> {
        rbCmd.command = CmdType.NEW_ORDER;
        rbCmd.timestamp = newOrder.timestamp;
        rbCmd.mid = newOrder.mid;
        rbCmd.uid = newOrder.uid;
        rbCmd.code = newOrder.code;
        rbCmd.direction = newOrder.direction;
        rbCmd.price = newOrder.price;
        rbCmd.volume = newOrder.volume;
        rbCmd.orderType = newOrder.orderType;
        rbCmd.oid = newOrder.oid;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };

    /**
     * 撤单trans
     */
    private static final EventTranslatorOneArg<RbCmd, OrderCmd> CANCEL_ORDER_TRANSLATOR = (rbCmd, seq, cancelOrder) -> {
        rbCmd.command = CmdType.CANCEL_ORDER;
        rbCmd.timestamp = cancelOrder.timestamp;
        rbCmd.mid = cancelOrder.mid;
        rbCmd.uid = cancelOrder.uid;
        rbCmd.code = cancelOrder.code;
        rbCmd.oid = cancelOrder.oid;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };

    /**
     * 行情发送
     */
    private static final EventTranslatorOneArg<RbCmd, OrderCmd> HQ_PUB_TRANSLATOR = (rbCmd, seq, hqPub) -> {
        rbCmd.command = CmdType.HQ_PUB;
        rbCmd.resultCode = CmdResultCode.SUCCESS;
    };
}
