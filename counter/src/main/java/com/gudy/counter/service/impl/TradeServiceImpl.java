package com.gudy.counter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gudy.counter.bean.Trade;
import com.gudy.counter.cache.CacheType;
import com.gudy.counter.cache.RedisCache;
import com.gudy.counter.mapper.TradeMapper;
import com.gudy.counter.service.TradeService;
import com.gudy.counter.thirdpart.hq.MatchData;
import com.gudy.counter.thirdpart.order.OrderCmd;
import com.gudy.counter.util.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:36
 */
@Service
public class TradeServiceImpl extends ServiceImpl<TradeMapper, Trade> implements TradeService {
    @Resource
    private TradeMapper tradeMapper;

    List<Trade> queryTrade(Long uid) {
        return tradeMapper.queryTrade(uid);
    }

    @Override
    public void saveTrade(int counterOId, MatchData md, OrderCmd orderCmd) {
        if (orderCmd == null) {
            return;
        }
        Map<String, Object> param = Maps.newHashMap();

        Trade trade = new Trade();

        trade.setId(md.tid);
        trade.setUid(orderCmd.uid);
        trade.setCode(orderCmd.code);
        trade.setDirection(orderCmd.direction.getDirection());
        trade.setPrice(md.price);
        trade.setTradeCount(md.volume);
        trade.setOrderId(counterOId);//高位为counterid
        trade.setTradeDate(Instant.ofEpochSecond(md.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        trade.setCreateTime(LocalDateTime.now());
        trade.setUpdateTime(LocalDateTime.now());

        tradeMapper.insert(trade);

        //更新缓存
        RedisCache.remove(Long.toString(orderCmd.uid), CacheType.TRADE);

    }

    @Override
    public List<Trade> getTradeList(long uid) {
        //查缓存
        String suid = Long.toString(uid);
        String tradeS = RedisCache.get(suid, CacheType.TRADE);
        if (StringUtils.isEmpty(tradeS)) {
            //未查到 查库
            List<Trade> tmp = queryTrade(uid);
            List<Trade> result = CollectionUtils.isEmpty(tmp) ? Lists.newArrayList() : tmp;
            //更新缓存
            RedisCache.cache(suid, JsonUtil.toJson(result), CacheType.TRADE);
            return result;
        } else {
            //查到 命中缓存
            return JsonUtil.fromJsonArr(tradeS, Trade.class);
        }
    }
}
