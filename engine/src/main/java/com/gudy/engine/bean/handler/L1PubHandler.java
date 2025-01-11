package com.gudy.engine.bean.handler;

import com.gudy.engine.bean.command.RingBufferCmd;
import com.gudy.engine.bean.orderbook.MatchEvent;
import com.gudy.engine.core.EngineCore;
import com.gudy.engine.thirdpart.bean.CommonMsg;
import com.gudy.engine.thirdpart.hq.L1MarketData;
import com.gudy.engine.thirdpart.hq.MatchData;
import com.gudy.engine.thirdpart.order.CmdType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.api.tuple.primitive.ShortObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.gudy.engine.thirdpart.bean.MsgConstants.*;


/**
 * 广播行情
 * 给每一个柜台，柜台发来的委托有成交变化的话，推送给对饮的个柜台
 */
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class L1PubHandler extends BaseHandler {
    /**
     * 发布行情的频率 往外推送 5档行情 和 发生变化的成交
     */
    public static final int HQ_PUB_RATE = 1000;
    /**
     * 指定 往这个地址上发送的消息 就是五档行情
     */
    public static final short HQ_ADDRESS = -1;
    /**
     * key 每一个柜台的id
     * value 发送对应的柜台数据(特别是成交数据) 随着行情一起发布 ，不会是一次成交发一次
     */
    private ShortObjectHashMap<List<MatchData>> matcherEventMap;

    private EngineCore engineCore;


    @Override
    public void onEvent(RingBufferCmd cmd, long sequence, boolean endOfBatch) throws Exception {
        final CmdType cmdType = cmd.command;
        /**
         * 如果是 新的委托 或者 撤单委托
         */
        if (cmdType == CmdType.NEW_ORDER || cmdType == CmdType.CANCEL_ORDER) {
            // 把这个委托加入到map
            // 这个cmd.matchEventList是撮合处理器生成出来的
            for (MatchEvent e : cmd.matchEventList) {
                // 根据会员号(柜台号) 放入对应柜台的数据
                matcherEventMap.get(e.mid).add(e.copy());
            }
        } else if (cmdType == CmdType.HQ_PUB) {// 如果是行情指令 说明 要发送行情
            //1.五档行情(放在cmd.marketDataMap中) 这个是所有柜台都收到的
            pubMarketData(cmd.marketDataMap);
            //2.给指定的柜台发送MatchData，哪一个柜台发来的委托，再给这个柜台单独发消息
            pubMatcherData();
        }

    }

    private void pubMatcherData() {
        if (matcherEventMap.size() == 0) {
            return;
        }

        log.info(matcherEventMap);

        try {
            for (ShortObjectPair<List<MatchData>> s : matcherEventMap.keyValuesView()) {
                // 如果某一个柜台的对应的list为空 没有消息发送 就跳过
                if (CollectionUtils.isEmpty(s.getTwo())) {
                    continue;
                }
                // 序列化要发送的数据
                byte[] serialize = engineCore.getBodyCodec().serialize(s.getTwo().toArray(new MatchData[0]));
                // 要发送的数据 柜台id 消息类型
                pubData(serialize, s.getOne(), MATCH_ORDER_DATA);
                // 发送完数据 清空已发送数据
                s.getTwo().clear();
            }
        } catch (Exception e) {
            log.error(e);
        }

    }

    /**
     * 发送五档行情(放在cmd.marketDataMap中) 这个是所有柜台都收到的
     *
     * @param marketDataMap
     */
    private void pubMarketData(IntObjectHashMap<L1MarketData> marketDataMap) {
        log.info(marketDataMap);
        byte[] serialize = null;
        try {
            serialize = engineCore.getBodyCodec().serialize(marketDataMap.values().toArray(new L1MarketData[0]));
        } catch (Exception e) {
            log.error(e);
        }
        if (serialize == null) {//序列化之后的数据位空
            return;
        }
        pubData(serialize, HQ_ADDRESS, MATCH_HQ_DATA);
    }

    /**
     * @param serialize 发送的数据
     * @param dst       发送目的地
     * @param msgType   发送的消息类型
     */
    private void pubData(byte[] serialize, short dst, short msgType) {
        /**
         * 所有总线的消息都是CommonMsg
         */
        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(serialize.length);
        msg.setChecksum(engineCore.getCs().getChecksum(serialize));
        msg.setMsgSrc(engineCore.getEngineConfig().getId());
        msg.setMsgDst(dst);
        msg.setMsgType(msgType);
        msg.setStatus(NORMAL);
        msg.setBody(serialize);
        engineCore.getBusSender().publish(msg);
    }
}
