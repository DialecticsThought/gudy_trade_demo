package com.gudy.counter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gudy.counter.bean.Trade;
import com.gudy.counter.thirdpart.hq.MatchData;
import com.gudy.counter.thirdpart.order.OrderCmd;

import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:35
 */
public interface TradeService extends IService<Trade> {
    void saveTrade(int counterOId, MatchData md, OrderCmd orderCmd);

    List<Trade> getTradeList(long uid);
}
