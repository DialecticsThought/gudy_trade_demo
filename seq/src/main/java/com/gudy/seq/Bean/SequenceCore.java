package com.gudy.seq.Bean;

import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MemoryDBOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gudy.seq.config.SequenceConfig;
import com.gudy.seq.task.FetchTask;
import com.gudy.seq.thirdpart.codec.IBodyCodec;
import com.gudy.seq.thirdpart.fetchsurv.IFetchService;
import jakarta.annotation.Resource;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.listener.ChannelListener;

import java.util.Map;
import java.util.Timer;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/8 19:11
 */
@Component
@Log4j2
@Data
public class SequenceCore {
    @Resource
    private SequenceConfig sequenceConfig;

    private Node node;

    private String fetchurls;

    private IBodyCodec codec;

    private Map<String, IFetchService> fetchServiceMap = Maps.newConcurrentMap();

    public void startup() {
        //1..初始化kv store集群
        startSeqDbCluster();
        //3.初始化网关连接
        startupFetch();
    }

    /*
     * 启动 k v store
     * */
    private void startSeqDbCluster() {
        /*
         *  因为 我们只有一个raft集群
         * */
        PlacementDriverOptions placementDriverOptions = PlacementDriverOptionsConfigured
                .newConfigured()
                .withFake(true)
                .config();
        StoreEngineOptions storeEngineOptions = StoreEngineOptionsConfigured
                .newConfigured()
                .withStorageType(StorageType.Memory)
                .withMemoryDBOptions(MemoryDBOptionsConfigured.newConfigured().config())
                .withRaftDataPath(sequenceConfig.getDataPath())
                .withServerAddress(new Endpoint(sequenceConfig.getServeIp(), sequenceConfig.getServerPort()))
                .config();

        RheaKVStoreOptions options = RheaKVStoreOptionsConfigured
                .newConfigured()
                .withInitialServerList(sequenceConfig.getServerList())
                .withStoreEngineOptions(storeEngineOptions)
                .withPlacementDriverOptions(placementDriverOptions).config();

        node = new Node(options);
        // 启动节点
        node.start();
        // 把raft集群节点的停止 和系统的停止关联起来
        Runtime.getRuntime().addShutdownHook(new Thread(node::stop));

        log.info("start seq node successfully on port : {}", sequenceConfig.getServerPort());
    }

    /*
     * 1.从哪些网关抓取
     * 2.通信方式
     * TODO
     *  这里 网关是服务端 因为 排队机是主动作为客户端向 网关要数据
     *  查看gateway 模块的 gatewayCore 的 initFetchServ()
     * */
    private void startupFetch() {
        // 1.建立所有到网关的连接
        // 获取从配置中传入的网关地址列表，假设是一个分号分隔的字符串
        String[] urls = fetchurls.split(";");
        for (String url : urls) {
            // 创建一个 ConsumerConfig 对象，配置 SOFARPC 客户端
            ConsumerConfig<IFetchService> consumerConfig = new ConsumerConfig<IFetchService>()
                    // 设置 RPC 通信接口，表示客户端请求的服务接口
                    .setInterfaceId(IFetchService.class.getName())
                    // 设置通信协议，这里使用了 SOFARPC 的 Bolt 协议
                    .setProtocol("bolt")// RPC通信协议
                    // 设置超时时间为 5000 毫秒（5秒）
                    .setTimeout(5000)
                    // 设置直连地址，也就是指定的网关地址（url）
                    .setDirectUrl(url);
            // 为 ConsumerConfig 配置连接成功后的回调监听器，这里将 FetchChannelListener 添加到 onConnect
            consumerConfig.setOnConnect(Lists.newArrayList(new FetchChannelListener(consumerConfig)));
            // 将每个网关的 ConsumerConfig 配置和服务引用放入 fetchServiceMap 中，key 为网关地址
            fetchServiceMap.put(url, consumerConfig.refer());
        }

        //2.定时取网关抓取数据的任务  每隔1s做
        new Timer().schedule(new FetchTask(this), 5000, 1000);
    }

    /*
     * 定义一个 FetchChannelListener，监听连接与断开事件法
     * */
    @AllArgsConstructor
    @NoArgsConstructor
    private class FetchChannelListener implements ChannelListener {
        /*
         * ProviderConfig：用于配置和暴露 服务端（服务提供者）。
         * ConsumerConfig：用于配置和调用 客户端（服务消费者）。
         * */
        private ConsumerConfig<IFetchService> config;

        /*
         * 当与网关(服务端)建立连接时，执行监听器中的 onConnected 方
         * */
        @Override
        public void onConnected(com.alipay.sofa.rpc.transport.AbstractChannel abstractChannel) {
            // 获取远程服务器地址，并打印连接成功日志
            String remoteAddr = abstractChannel.remoteAddress().toString();
            log.info("connect to gatewat : {}", remoteAddr);
            // 将网关地址和其对应的服务引用添加到 fetchServiceMap 中
            fetchServiceMap.put(remoteAddr, config.refer());
        }

        /*
         * 当与网关(服务端)断开连接时，断开连接时回调的方法
         * */
        @Override
        public void onDisconnected(com.alipay.sofa.rpc.transport.AbstractChannel abstractChannel) {
            // 获取远程服务器地址，并打印断开连接日志
            String remoteAddr = abstractChannel.remoteAddress().toString();
            log.info("disconnect from gatewat : {}", remoteAddr);
            // 从 fetchServiceMap 中移除已断开连接的网关地址
            fetchServiceMap.remove(remoteAddr);
        }
    }
}
