package com.gudy.counter.thirdpart.codec;

import com.gudy.counter.thirdpart.bean.CommonMsg;
import io.vertx.core.buffer.Buffer;

/**
 * @Description
 * TODO commMsg 和 TCP流 也就是序列化字节 之间的转换
 * @Author veritas
 * @Data 2025/1/7 19:42
 */
public interface IMsgCodec {
    /*
    * tcp -> commonMsg
    * */
    Buffer encodeToBuffer(CommonMsg msg);
    /*
    * commonMsg -> tcp
    * */
    CommonMsg decodeFromBuffer(Buffer buffer);
}
