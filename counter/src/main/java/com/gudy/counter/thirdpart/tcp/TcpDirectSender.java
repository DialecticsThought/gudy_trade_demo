package com.gudy.counter.thirdpart.tcp;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.net.NetSocket;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import io.vertx.core.buffer.Buffer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Description 柜台作为客户端 与 网关作为服务端的tcp的连接的类
 * @Author veritas
 * @Data 2025/1/7 20:03
 */
@Log4j2
@NoArgsConstructor
public class TcpDirectSender {
    /*
     * 网关ip
     * */
    private String ip;
    /*
     * 网关端口
     * */
    private int port;
    /**
     * 用来创建tcp连接的类
     */
    private io.vertx.core.Vertx vertx;

    /*
     * TODO 每次取socket对象的时候是取内存 而不是线程工作缓存的内存 保证最新的
     * */
    private volatile NetSocket socket;
    /*
     * 不需要来一条数据 就利用socket发送一条数据
     * 而是设置一个缓存，所有需要发送的数据(序列化之后的字节流) 放在缓存中，让socket取缓存的数据
     * 看下面线程
     * */
    private final BlockingQueue<Buffer> sendCache = new LinkedBlockingQueue<>();

    public TcpDirectSender(String gatewayIp, int gatewayPort, io.vertx.core.Vertx vertx) {
        this.ip = gatewayIp;
        this.port = gatewayPort;
        this.vertx = vertx;
    }

    /*
     * 发送数据 的方法 本质就是 把要发送的数据放入缓存
     * */
    public boolean send(Buffer bufferMsg) {
        return sendCache.offer(bufferMsg);
    }


    /*
     * TODO 创建 客户端 与网关的通信
     * */
    public void startup() {
        vertx.createNetClient().connect(port, ip, new ClientConnHandler());

        /*
         * 真正把报文发送给网关的 事情 有专门的线程
         * 这个线程 不断轮询 缓存 有数据 就发送
         * */
        new Thread(() -> {
            while (true) {
                try {
                    // 每次阻塞5s 还没有数据 poll方法返回一个null
                    // 如果有数据 就会继续执行
                    Buffer msgBuffer = sendCache.poll(5, TimeUnit.SECONDS);
                    if (msgBuffer != null && msgBuffer.length() > 0 && socket != null) {
                        log.info("before send msg: " + msgBuffer.toString());
                        socket.write(msgBuffer);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    private class ClientConnHandler implements Handler<AsyncResult<NetSocket>> {
        /*
         * TODO 为了把 网关 -> 柜台 ，柜台 -> 网关 这两个流 分开来 也就是异步
         * 所以需要一个缓存
         * */
        @Override
        public void handle(AsyncResult<NetSocket> netSocketAsyncResult) {
            if (netSocketAsyncResult.succeeded()) {
                log.info("connected to " + ip + ":" + port);
                // TODO 连接成功 能得到一个链接对象
                socket = netSocketAsyncResult.result();
                // 设置 该链接的 关闭的处理器
                socket.closeHandler(close -> {
                    log.info("closed " + ip + ":" + port);
                    // 关于 重新连接的逻辑
                    reconnect();
                });
                // 设置 该链接的 异常的处理器
                socket.exceptionHandler(exception -> {
                    log.error("exception " + ip + ":" + port, exception);
                });
            } else {

            }
        }

        /*
         * 每隔五秒钟 尝试重新连接
         * */
        private void reconnect() {
            vertx.setTimer(1000 * 5, r -> {
                vertx.createNetClient().connect(port, ip, new ClientConnHandler());
            });
        }
    }
}
