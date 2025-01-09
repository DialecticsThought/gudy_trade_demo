package com.gudy.engine.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/9 14:44
 */
@Configuration
@Data
public class EngineConfig {
    /**
     * 撮合核心模块自带的id
     */
    private short id;
    /**
     * mqtt总线的端口
     * engine 会把数据写入总线 其他模块订阅
     */
    @Value("${engine.pub-ip}")
    private String pubIp;
    /**
     * mqtt总线的地址
     */
    @Value("${engine.pub-port}")
    private Integer pubPort;
    /**
     * 组播端口 和sequence模块关联
     */
    @Value("${engine.order-recv-port}")
    private Integer orderRecvPort;
    /**
     * 组播地址 和sequence模块关联
     */
    @Value("${engine.order-recv-ip}")
    private String orderRecvIp;
    /**
     * raft的kv数据库的集群的ip+port列表
     */
    @Value("${engine.sequence-list}")
    private String sequenceList;
}
