package com.gudy.counter.thirdpart.codec;

import com.gudy.counter.thirdpart.bean.CommonMsg;
import io.vertx.core.buffer.Buffer;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/7 19:54
 */
public class MsgCodec implements IMsgCodec {
    /*
    * TODO 变成字节 和字节变成对象 的顺序需要相同
    * */
    @Override
    public Buffer encodeToBuffer(CommonMsg msg) {
        return Buffer.buffer()
                .appendInt(msg.getBodyLength())
                .appendByte(msg.getChecksum())
                .appendShort(msg.getMsgSrc())
                .appendShort(msg.getMsgDst())
                .appendShort(msg.getMsgType())
                .appendByte(msg.getStatus())
                .appendLong(msg.getMsgNo())
                .appendBytes(msg.getBody());
    }

    /*
     * TODO 变成字节 和字节变成对象 的顺序需要相同
     * TODO 网关部分 也要和这个相同
     * */
    @Override
    public CommonMsg decodeFromBuffer(Buffer buffer) {
        int bodyLength = buffer.getInt(0);
        byte checksum = buffer.getByte(4);
        short msgSrc = buffer.getShort(5);
        short msgDst = buffer.getShort(7);
        short msgType = buffer.getShort(9);
        byte status = buffer.getByte(11);
        long packetNo = buffer.getLong(12);
        byte[] bodyBytes = buffer.getBytes(20, 20 + bodyLength);

        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(bodyBytes.length);
        msg.setChecksum(checksum);
        msg.setMsgSrc(msgSrc);
        msg.setMsgDst(msgDst);
        msg.setMsgType(msgType);
        msg.setStatus(status);
        msg.setBody(bodyBytes);
        msg.setMsgNo(packetNo);
        return msg;
    }
}
