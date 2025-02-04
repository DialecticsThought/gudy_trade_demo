package com.gudy.counter.config;

import com.gudy.counter.consumer.MQTTBusConsumer;
import com.gudy.counter.thirdpart.checksum.ICheckSum;
import com.gudy.counter.thirdpart.codec.BodyCodec;
import com.gudy.counter.thirdpart.codec.MsgCodec;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.zip.Checksum;


/**
 * @Description 柜台配置类
 * @Author veritas
 * @Data 2025/1/5 20:00
 */
@Component
@Data
public class CounterConfig {
    private static final Logger log = LoggerFactory.getLogger(CounterConfig.class);

    /**
     *  柜台和委托中端,通信的websocket的地址
     */
    @Value("${counter.publish_port}")
    private int pubPort;

    /**
     * 订阅的总线的ip
     */
    @Value("${counter.subscribe_bus_ip}")
    private String subBusIp;

    /**
     * 订阅的总线的port
     */
    @Value("${counter.subscribe_bus_port}")
    private int subBusPort;

    /**
     * 柜台号 也就是会员号
     */
    @Value("${counter.id}")
    private short memId;
    /**
     * 雪花算法用到的
     */
    @Value("${counter.dataCenterId}")
    private Long dataCenterId;
    /**
     * 雪花算法用到的
     */
    @Value("${counter.rackId}")
    private Long workerId;
    /*
     * 网关ip
     * */
    @Value("${counter.gateway_ip}")
    private String gatewayIp;
    /*
     * 网关端口
     * */
    @Value("${counter.gateway_port}")
    private Integer gatewayPort;

    @Value("${counter.gatewayid}")
    private short gatewayId;
    /*
     * 校验和相关的实现类的类路径
     * */
    @Value("${counter.checksum}")
    private String checkSumClass;
    /*
     * 对象序列化的实现类的类路径
     * */
    @Value("${counter.bodycodec}")
    private String bodyCodecClass;
    /*
     * 对象序列化的实现类的类路径
     * */
    @Value("${counter.msgcodec}")
    private String msgCodecClass;
    /*
     * 校验和相关的实现类
     * */
    private ICheckSum cs;
    /*
     * 对象序列化的实现类
     * */
    private BodyCodec bodyCodec;
    /*
     * 消息 与字节流转换器
     * */
    private MsgCodec msgCodec;
    /*
     * vertx上下文
     * */
    private io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();

    @PostConstruct
    private void init() {
        Class<?> clazz;

        try {
            clazz = Class.forName(checkSumClass);
            cs = (ICheckSum) clazz.getDeclaredConstructor().newInstance();

            clazz = Class.forName(bodyCodecClass);
            bodyCodec = (BodyCodec) clazz.getDeclaredConstructor().newInstance();

            clazz = Class.forName(msgCodecClass);
            msgCodec = (MsgCodec) clazz.getDeclaredConstructor().newInstance();

            // 对于这个BusConsumer的初始化,
            // 它是要有一个顺序依赖关系的。
            // 它是需要在CounterConfiguration这个类,初始化完成之后才能执行的
            // 初始化总线连接
            new MQTTBusConsumer(subBusIp, subBusPort, String.valueOf(memId), msgCodec, cs, vertx).startup();
        } catch (Exception e) {
            log.info("init config err: {}", e);
        }
    }

}
