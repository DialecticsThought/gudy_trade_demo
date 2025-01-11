package com.gudy.engine.thirdpart.bus;

import com.gudy.engine.thirdpart.bean.CommonMsg;
import com.gudy.engine.thirdpart.codec.IMsgCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;


import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


import java.util.concurrent.TimeUnit;

/**
 * 基于mqtt协议的总线
 */
@Log4j2
@NoArgsConstructor
public class MqttBusSender implements IBusSender {
    /**
     * 总线的ip
     */
    private String ip;
    /**
     * 总线的端口
     */
    private Integer port;
    /**
     * 消息的编码类
     */
    private IMsgCodec msgCodec;
    /**
     * 总线
     */
    private Vertx vertx;

    public MqttBusSender(String pubIp, Integer pubPort, IMsgCodec msgCodec, Vertx vertx) {
        this.ip = pubIp;
        this.port = pubPort;
        this.msgCodec = msgCodec;
        this.vertx = vertx;
    }

    /**
     * 启动(连接)总线
     */
    @Override
    public void startup() {
        //连接总线
        mqttConnect();
    }

    private void mqttConnect() {
        // 创建一个mqtt总线客户端
        MqttClient mqttClient = MqttClient.create(vertx);
        mqttClient.connect(port, ip, res -> {
            // 连接成功的回调方法
            if (res.succeeded()) {
                log.info("connect to mqtt bus[ip:{},port:{}] succeed", ip, port);
                sender = mqttClient;
            } else { // 连接失败的回调方法
                log.info("connect to mqtt bus[ip:{},port:{}] fail", ip, port);
                // 重连
                mqttConnect();
            }
        });

        /**
         * 与总线意外断开 也要重连
         */
        mqttClient.closeHandler(h -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error(e);
            }
            mqttConnect();
        });
    }

    ///////////////////////////////////////////////////////
    private volatile MqttClient sender;

    @Override
    public void publish(CommonMsg msg) {
        //发往每一个柜台 每一个柜台有short类型的id
        // 柜台启动 也会根据id注册到总线 订阅消息
        sender.publish(Short.toString(msg.getMsgDst()),
                msgCodec.encodeToBuffer(msg),//编码后的消息
                MqttQoS.AT_LEAST_ONCE,// 消息至少到达一次
                false,//不判断是否重复
                false);// 总线不保留数据
    }
}
