package com.gudy.seq.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/8 18:33
 */
@Configuration
@Data
public class SequenceConfig {
    /*
     * 存放raft数据的本地的路径
     * */
    @Value("${raft.datapath}")
    private String dataPath;
    /*
     * raft集群的节点在哪一个url上提供服务
     * */
    @Value("${raft.serverip}")
    private String serveIp;
    /*
     * raft集群的节点在哪一个url上提供服务
     * */
    @Value("${raft.serverport}")
    private Integer serverPort;
    /*
     * 当前raft集群的服务
     * */
    @Value("${raft.serverlist}")
    private String serverList;

}
