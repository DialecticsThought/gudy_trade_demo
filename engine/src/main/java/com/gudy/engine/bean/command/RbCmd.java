package com.gudy.engine.bean.command;

import com.gudy.engine.bean.orderbook.MatchEvent;
import com.gudy.engine.thirdpart.hq.L1MarketData;
import com.gudy.engine.thirdpart.order.CmdType;
import com.gudy.engine.thirdpart.order.OrderDirection;
import com.gudy.engine.thirdpart.order.OrderType;
import lombok.Builder;
import lombok.ToString;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.List;

@Builder
@ToString
public class RbCmd {

    public long timestamp;

    public short mid;

    public long uid;

    public CmdType command;

    public int code;

    public OrderDirection direction;

    public long price;

    public long volume;

    public long oid;

    public OrderType orderType;

    // 保存撮合结果
    public List<MatchEvent> matchEventList;

    // 前置风控 --> 撮合 --> 发布
    public CmdResultCode resultCode;

    // 保存行情
    public IntObjectHashMap<L1MarketData> marketDataMap;

}
