package com.gudy.gateway.bean;

import com.gudy.gateway.bean.handler.ConnectHandler;
import com.gudy.gateway.thirdpart.checksum.ICheckSum;
import com.gudy.gateway.thirdpart.codec.IBodyCodec;
import io.vertx.core.net.NetServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/7 13:39
 */
@Data
@Component
@Log4j2
public class GatewayConfig {
    /*
     *排队机抓取服务的端口
     * */
    @Value("${gateway.fetch-service-port}")
    private Integer fetchServicePort;
    /*
     * 网关id
     * */
    @Value("${gateway.id}")
    private Short id;
    /*
     * 和柜台counter连接的端口
     * */
    @Value("${gateway.recv-port}")
    private Integer recvPort;
    /*
     * 序列化和反序列化的解析器
     * */
    @Resource
    private IBodyCodec bodyCodec;
    /*
     * 校验和检查器
     * */
    @Resource
    private ICheckSum cs;
    /*
     * vertx上下文
     * */
    private io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();

}
