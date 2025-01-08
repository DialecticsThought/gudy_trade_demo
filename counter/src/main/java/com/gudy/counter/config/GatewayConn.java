package com.gudy.counter.config;

import com.gudy.counter.thirdpart.bean.CommonMsg;
import com.gudy.counter.thirdpart.bean.MsgConstants;
import com.gudy.counter.thirdpart.order.OrderCmd;
import com.gudy.counter.thirdpart.tcp.TcpDirectSender;
import com.gudy.counter.thirdpart.uuid.GudyUuid;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/7 19:23
 */
@Log4j2
@Configuration
public class GatewayConn {
    /*
     * 注入柜台配置类
     * */
    @Resource
    private CounterConfig config;
    @Resource
    private GudyUuid gudyUuid;

    private TcpDirectSender tcpDirectSender;

    /*
     * 初始化TcpDirectSender
     * */
    @PostConstruct
    private void init() {
        tcpDirectSender = new TcpDirectSender(config.getGatewayIp(), config.getGatewayPort(), config.getVertx());
        tcpDirectSender.startup();
    }

    /*
     * 发送委托
     *  把已经序列化的orderCmd方法 封装成 commonMsg
     * */

    public void sendOrder(OrderCmd orderCmd) {
        byte[] data = null;
        try {
            data = config.getBodyCodec().serialize(orderCmd);
        } catch (Exception e) {
            log.error("encode error form ordercmd : {}", orderCmd, e);
        }
        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(data.length);
        msg.setChecksum(config.getCs().getChecksum(data));
        // TODO 原地址 是 柜台id
        msg.setMsgSrc(config.getMemId());
        // TODO 目标地址 是网关ip
        msg.setMsgDst(config.getGatewayId());
        msg.setMsgType(MsgConstants.COUNTER_NEW_ORDER);
        msg.setStatus(MsgConstants.NORMAL);
        msg.setMsgNo(gudyUuid.getUUID());
        msg.setBody(data);

        tcpDirectSender.send(config.getMsgCodec().encodeToBuffer(msg));
    }
}
