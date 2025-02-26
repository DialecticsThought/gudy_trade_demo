package com.gudy.counter.consumer;

import com.google.common.collect.Maps;
import com.gudy.counter.thirdpart.bean.CommonMsg;
import com.gudy.counter.thirdpart.checksum.ICheckSum;
import com.gudy.counter.thirdpart.codec.IMsgCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.gudy.counter.thirdpart.bean.MsgConstants.MATCH_HQ_DATA;
import static com.gudy.counter.thirdpart.bean.MsgConstants.MATCH_ORDER_DATA;

/**
 * @Description
 * @Author veritas
 * @Data 2025/2/4 16:56
 */
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class MQTTBusConsumer {

    /**
     *  订阅的总线的ip
     */
    private String busIp;

    /**
     * 当前柜台从总线 拿 match数据的地址
     */
    private int busPort;

    /**
     * 订阅的总线的port
     */
    private String recvAddr;

    /**
     * 序列化工具类
     */
    private IMsgCodec msgCodec;

    /**
     * 校验工具类
     */
    private ICheckSum cs;

    /**
     * vertx注入
     */
    private Vertx vertx;

    private final static String HQ_ADDR ="-1";

    public static final String INNER_MARKET_DATA_CACHE_ADDR = "l1_market_data_cache_addr";

    public static final String INNER_MATCH_DATA_ADDR = "match_data_addr";

    public void startup(){
        mqttConnect(vertx, busIp,busPort );
    }

    public void mqttConnect(Vertx vertx, String busIp, int busPort){
        MqttClient mqttClient = MqttClient.create(vertx);

        mqttClient.connect(busPort,busIp,response ->{
            if(response.succeeded()){
                log.info("connect mqtt bus succeed");
                Map<String,Integer> topics = Maps.newHashMap();
                // 监听柜台的id
                topics.put(recvAddr, MqttQoS.AT_LEAST_ONCE.value());
                // 监听 所有的柜台都有的行情地址 行情地址 = -1
                topics.put(HQ_ADDR,MqttQoS.AT_MOST_ONCE.value());

                mqttClient.subscribe(topics);

                mqttClient.publishHandler(h -> {
                            // 数据就在handler的payload 传输的数据是MqttPublishMessage类型
                            CommonMsg msg = msgCodec.decodeFromBuffer(h.payload());
                            // 检查校验和
                            if (msg.getChecksum() != (cs.getChecksum(msg.getBody()))) {
                                return;
                            }
                            byte[] body = msg.getBody();
                            if (ArrayUtils.isNotEmpty(body)) {
                                short msgType = msg.getMsgType();
                                // 帮助去重，为了演示方便 不做校验
                                //long msgNo = msg.getMsgNo();

                                // TODO 这里只是处理两种数据
                                // 订阅处理器收到消息后 并不是进行真正的解码工作,
                                // 而是把编解码的工作放给行情处理器和Match处理器来做。
                                // 要使用TCP的消息总线, 就直接使用vertx的event bus,就可以获得这个消息总线了。
                                // 丢数据的逻辑, 跟mqtt的总线在vertx当中 定义的API是一模一样的。
                                //TODO 查看 五档行情处理器和match处理器这两个类
                                if (msgType == MATCH_ORDER_DATA) {// 匹配的数据 有些客户的委托是该柜台发出的话 会收到 对应的消息
                                    vertx.eventBus().send(INNER_MATCH_DATA_ADDR, Buffer.buffer(body));
                                } else if (msgType == MATCH_HQ_DATA) {// 五档行情数据
                                    vertx.eventBus().send(INNER_MARKET_DATA_CACHE_ADDR, Buffer.buffer(body));
                                } else {// 未知的数据类型 记日志
                                    log.error("recv unknown msgType:{}", msg);
                                }
                            }
                        });
            }else {
                log.error("connect mqtt bus failed");
            }
        });


        mqttClient.closeHandler(h -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error(e);
            }
            mqttConnect(vertx,busIp , busPort);
        });
    }
}
