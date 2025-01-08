package com.gudy.gateway.bean.core;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.gudy.gateway.bean.GatewayConfig;
import com.gudy.gateway.bean.OrderCmdContainer;
import com.gudy.gateway.bean.handler.ConnectHandler;
import com.gudy.gateway.thirdpart.fetchsurv.IFetchService;
import io.vertx.core.net.NetServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Description
 * <pre>
 *     tcp        sofa
 * 柜台 ----> 网关 ----> 排队机
 * </pre>
 * @Author veritas
 * @Data 2025/1/8 20:04
 */
@Component
@Log4j2
public class GatewayCore {

    @Resource
    private GatewayConfig gatewayConfig;
    @Resource
    private OrderCmdContainer orderCmdContainer;
    /*
     * 解析xml配置文件
     * */
    @Deprecated
    public void initAndParseConfig(String filePath) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(filePath));
        // 得到根节点
        Element rootElement = document.getRootElement();
        //解析得到端口 和网关id
/*        id = Short.parseShort(rootElement.element("id").getText());
        recvPort = Integer.parseInt(rootElement.element("recvPort").getText());*/

        //TODO 数据库连接 连接柜台列表
    }

    @PostConstruct
    private void init() {
        startup();
    }

    public void startup() {
        //启动TCP监听对应的revcPort端口
        initRecv();
        //2.排队机交互
        initFetchServ();
    }

    public void initRecv() {
        NetServer netServer = gatewayConfig.getVertx().createNetServer();
        netServer.connectHandler(new ConnectHandler(gatewayConfig));
        netServer.listen(gatewayConfig.getRecvPort(), asyncResult -> {
            if (asyncResult.succeeded()) {
                log.info("gateway startup success at port : {}", gatewayConfig.getRecvPort());
            } else {
                log.error("gateway startup fail");
            }
        });
    }

    /*
    * TODO 这里 网关是服务端 因为 排队机是主动作为客户端向 网关要数据
    * */
    private void initFetchServ() {
        /*
        * 创建一个 ServerConfig 对象，配置服务端
        * 指定 通信的端口
        * 指定通信的协议
        * */
        ServerConfig rpcConfig = new ServerConfig()
                .setPort(gatewayConfig.getFetchServicePort())
                .setProtocol("bolt");
        // ProviderConfig 用于配置暴露的服务，IFetchService 是服务的接口类型
        // TODO 可以理解为servlet 定义一处理请求的服务
        ProviderConfig<IFetchService> providerConfig = new ProviderConfig<IFetchService>()
                // 设置服务的接口标识符 这里是 IFetchService 接口
                .setInterfaceId(IFetchService.class.getName())
                // 设置服务实现的引用，这里使用 Lambda 表达式来提供服务实现类
                // 相当于 orderCmdContainer.getAll() 返回的是 IFetchService 的实现
                .setRef(() -> orderCmdContainer.getAll())
                // 设置服务端的配置，传入之前创建的 rpcConfig（ServerConfig 对象）
                .setServer(rpcConfig);
        // 方法将服务暴露出去，客户端可以通过 SOFARPC 的注册中心或直连的方式访问这个服务
        providerConfig.export();

        log.info("gateway startup fetchServ success at port : {}", gatewayConfig.getFetchServicePort());

    }
}
