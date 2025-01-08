package com.gudy.gateway.bean.handler;

import com.gudy.gateway.bean.OrderCmdContainer;
import com.gudy.gateway.thirdpart.bean.CommonMsg;
import com.gudy.gateway.thirdpart.codec.IBodyCodec;
import com.gudy.gateway.thirdpart.order.OrderCmd;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.checkerframework.checker.units.qual.N;


/**
 * @Description 网关作为中介，传递柜台的委托和其他信息
 * 所以网关需要一个缓存，把委托信息缓存下来，提供给排队机或者其他模块
 * 写缓存的操作一定是异步的，不能干扰到tcp的连接的处理
 * @Author veritas
 * @Data 2025/1/7 17:37
 */
@Log4j2
@NoArgsConstructor
public class MsgHandlerImpl implements MsgHandler {
    /*
     * 序列化器
     *  */
    private IBodyCodec bodyCodec;
    @Resource
    private OrderCmdContainer orderCmdContainer;

    public MsgHandlerImpl(IBodyCodec bodyCodec) {
        this.bodyCodec = bodyCodec;
    }

    @Override
    public void onCounterData(CommonMsg msg) {

        OrderCmd orderCmd;

        try {
            orderCmd = bodyCodec.deserialize(msg.getBody(), OrderCmd.class);

            log.info("receive order cmd : {}", orderCmd);

/*            if (log.isDebugEnabled()) {// 判断是否开启了debug

            }*/

            if(!orderCmdContainer.cache(orderCmd)){
                // 如果因为内存问题导致没有缓存成功
                log.error("gateway queue insery fail : {} , queu.length: {} ",orderCmd,orderCmdContainer.size());
            }
        } catch (Exception e) {
            log.error("deserialize order cmd failed", e);
            throw new RuntimeException(e);
        }

    }
}
