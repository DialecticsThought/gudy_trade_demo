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
     * 网关id
     * */
    @Value("${gateway.id}")
    private Short id;
    /*
     * 连接的端口
     * */
    @Value("${server.port}")
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

    /*
     * 解析xml配置文件
     * */
    public void initAndParseConfig(String filePath) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(filePath));
        // 得到根节点
        Element rootElement = document.getRootElement();
        //解析得到端口 和网关id
        id = Short.parseShort(rootElement.element("id").getText());

        recvPort = Integer.parseInt(rootElement.element("recvPort").getText());

        //TODO 数据库连接 连接柜台列表
    }

    public void startup() {
        //启动TCP监听对应的revcPort端口
        initRecv();

        //TODO 排队机交互
    }

    public void initRecv() {
        NetServer netServer = vertx.createNetServer();
        netServer.connectHandler(new ConnectHandler(this));
        netServer.listen(recvPort, asyncResult -> {
            if (asyncResult.succeeded()) {
                log.info("gateway startup success at port : {}", recvPort);
            } else {
                log.error("gateway startup fail");
            }
        });
    }

    @PostConstruct
    private void init() {
        startup();
    }
}
