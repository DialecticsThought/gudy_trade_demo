package com.gudy.counter.config;

import io.vertx.core.Vertx;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 和柜台的config一样。
 * 要出使化这个基于websocket的消息总线,我们也写一个config类来管理这个初始化
 */
@Log4j2
@Configuration
public class WebSocketConfig {
    // 涉及到的请求类型有三种。
    // 分别是对于行情数据的请求,以及成交的通知,还有委托的通知
    // 进来的请求,就只有这个行情
    // 出去的请求有两种,一个是委托变动的通知,允许通过总线网外发,
    // 另外一个是成交变动的通知,允许网外发。
    // 其他类型的数据都是不允许往这个总线上面丢的。
    public static final String L1_MARKET_DATA_PREFIX = "l1-market-data";

    public final static String TRADE_NOTIFY_ADDR_PREFIX = "tradechange-";

    public final static String ORDER_NOTIFY_ADDR_PREFIX = "orderchange-";

    @Resource
    private CounterConfig config;

    @PostConstruct
    private void init() {
        // 要建立这个基于websocket的总线
        // 需要先拿到Vertx
        Vertx vertx = config.getVertx();

        //只允许成交 委托的变动通过websocket总线往外发送
        // 它需要指定通过这个websocket哪些请求可以放行进来 && 哪些请求可以放行出去
        // 这些请求都是有固定的格式的。
        // 我们先把这些格式作为一个长量,给它定义出来。
        BridgeOptions options = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress(L1_MARKET_DATA_PREFIX))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex(ORDER_NOTIFY_ADDR_PREFIX + "[0-9]+"  ))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex(TRADE_NOTIFY_ADDR_PREFIX + "[0-9]+"));
        // 通过Vertx来创建这样一个websocket的处理器
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        // 这个处理器需要指定两个参数。
        // 第一个,就是这个连接有哪些选项。
        // 第二个,就是对于连接事件的一些处理器。
        sockJSHandler.bridge(options, event -> {
            // 这里就做一个简单的打印了。
            // 当这个socket的创建和关闭的时候,我们都给它做个打印
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                log.info("client : {} connected", event.socket().remoteAddress());
            } else if (event.type() == BridgeEventType.SOCKET_CLOSED) {
                log.info("client : {} closed", event.socket().remoteAddress());
            }
            event.complete(true);
        });
        // 还是要指定它是在哪个URL上面对外提供服务
        // 这个URL的指定,是通过router来执行的
        Router router = Router.router(vertx);
        // 只要你的websocket的请求,是满足eventbus的前綴的,都可以让它连进来
        router.route("/eventbus/*").handler(sockJSHandler);
        // 最后再给它指定一个监听的端口
        vertx.createHttpServer().requestHandler(router).listen(config.getPubPort());
    }
}
