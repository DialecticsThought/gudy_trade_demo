package com.gudy.gateway.bean.handler;

import com.gudy.gateway.bean.GatewayConfig;
import com.gudy.gateway.thirdpart.bean.CommonMsg;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @Description 网关的 链接的handler
 * 网关 与 柜台之间通信，
 * 柜台相当于客户端 发起练级，网关相当于服务端
 * 柜台 打包报文 + 发送报文刚给网关
 * @Author veritas
 * @Data 2025/1/7 14:00
 */
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class ConnectHandler implements io.vertx.core.Handler<NetSocket> {

    private GatewayConfig config;
    /*
     * 包头： 包体长度int + 校验和 byte + src short + dst short +消息类型 short + 消息状态 byte + 报的编号 long
     * cs 校验
     * src = 来源 也就是柜台id
     * dst 目的地
     * type = 数据类型 根据不同的类型 采用不同的代码
     * status = 状态
     * */
    private int PACKET_HEADER_LENGTH = 4 + 1 + 2 + 2 + 2 + 1 + 8;

    public ConnectHandler(GatewayConfig gatewayConfig) {
        this.config = gatewayConfig;
    }

    @Override
    public void handle(NetSocket netSocket) {
        MsgHandler msgHandler = new MsgHandlerImpl(config.getBodyCodec());
        // 刚连接 做出连接的通知
        msgHandler.onConnect(netSocket);
        // 解析器
        RecordParser recordParser = RecordParser.newFixed(PACKET_HEADER_LENGTH);
        recordParser.setOutput(new io.vertx.core.Handler<Buffer>() {
            // 包体长度int + 校验和byte+src short + dst short +消息类型 short + 消息状态 byte + 报的编号 long
            // 初始值
            int bodyLength = -1;
            byte checkSum = -1;
            short msgSrc = -1;
            short msgDst = -1;
            short msgType = -1;
            byte status = -1;
            long packetNo = -1;

            /*
             * 这个方法自动循环
             * */
            @Override
            public void handle(Buffer buffer) {
                // 说明第一次读取到包
                if (bodyLength == -1) {
                    // TODO 下面的顺序和柜台module 中 把字节变成对象的方法的顺序相同
                    // 从0开始取
                    int bodyLength = buffer.getInt(0);
                    // 从第32位开始取
                    byte checkSum = buffer.getByte(4);

                    short msgSrc = buffer.getShort(5);

                    short msgDst = buffer.getShort(7);

                    short msgType = buffer.getShort(9);

                    byte status = buffer.getByte(11);

                    long packetNo = buffer.getLong(12);

                    //重新设置 获取数据的长度
                    // 也就是说下次读取TCP报文的时候，只有读取到了bodyLength之后才进入 handle方法
                    recordParser.fixedSizeMode(bodyLength);
                } else {
                    // 读取数据
                    byte[] bytes = buffer.getBytes();
                    // 组装对象
                    CommonMsg msg;
                    // 判断校验是否通过
                    if (checkSum != config.getCs().getChecksum(bytes)) {
                        log.info("illegal byte body exit from client:{}", netSocket.remoteAddress());
                        return;
                    } else {
                        // 判断数据包 是否真的发送给柜台
                        if (msgDst != config.getId()) {// 发送的柜台id 和本柜台id不同
                            log.error("recv error msgDst dist:{} from client:{}", msgDst, netSocket.remoteAddress());
                            return;
                        }
                        msg = new CommonMsg();
                        msg.setBodyLength(bodyLength);
                        msg.setChecksum(checkSum);
                        msg.setMsgSrc(msgSrc);
                        msg.setMsgDst(msgDst);
                        msg.setMsgType(msgType);
                        msg.setStatus(status);
                        msg.setMsgNo(packetNo);
                        msg.setBody(bytes);
                        msg.setTimestamp(System.currentTimeMillis());

                        // 通过消息处理器 做通知
                        msgHandler.onCounterData(msg);

                        // 恢复现场 为下一次解析做准备
                        int bodyLength = -1;
                        byte checkSum = -1;
                        short msgSrc = -1;
                        short msgDst = -1;
                        short msgType = -1;
                        byte status = -1;
                        long packetNo = -1;
                        recordParser.fixedSizeMode(PACKET_HEADER_LENGTH);
                    }
                }
            }
        });
        // 绑定解析器
        netSocket.handler(recordParser);
        // 异常处理器
        netSocket.exceptionHandler(exception -> {
            // 异常的通知
            msgHandler.onException(netSocket, exception);
        });
        // 退出处理器
        netSocket.closeHandler(close -> {
            // 退出链接的通知
            msgHandler.onDisconnect(netSocket);
        });

    }
}
