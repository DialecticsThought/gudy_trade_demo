package com.gudy.counter.config;

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

    @Value("${counter.id}")
    private short memId;

    @Value("${counter.dataCenterId}")
    private Long dataCenterId;

    @Value("${counter.rackId}")
    private Long workerId;
    /*
     * 网关ip
     * */
    @Value("${counter.gatewayIp}")
    private String gatewayIp;
    /*
     * 网关端口
     * */
    @Value("${counter.gatewayPort}")
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
        } catch (Exception e) {
            log.info("init config err: {}", e);
        }
    }

   /* public Long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(Long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public short getMemId() {
        return memId;
    }

    public void setMemId(short memId) {
        this.memId = memId;
    }*/
}
