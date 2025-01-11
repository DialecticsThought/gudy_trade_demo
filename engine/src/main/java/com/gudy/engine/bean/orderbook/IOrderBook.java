package com.gudy.engine.bean.orderbook;

import com.gudy.engine.bean.command.CmdResultCode;
import com.gudy.engine.bean.command.RingBufferCmd;
import com.gudy.engine.thirdpart.hq.L1MarketData;

import static com.gudy.engine.thirdpart.hq.L1MarketData.L1_SIZE;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/11 10:28
 */
public interface IOrderBook {
    /**
     * 新增委托
     *
     * @param cmd
     * @return
     */
    CmdResultCode newOrder(RingBufferCmd cmd);

    /**
     * 撤单(撤销委托)
     *
     * @param cmd
     * @return
     */
    CmdResultCode cancelOrder(RingBufferCmd cmd);

    /**
     * 查询行情的快照
     *
     * @return
     */
    default L1MarketData getL1MarketDataSnapshot() {
        // 构建L1MarketData该对象
        // 需要知道 实际行情 卖的orderBucket和买的orderBucket 分别有几档
        // 有了这2个size 就能根据实际的买卖档位数 来创建 L1MarketData对象
        final int buySize = limitBuyBucketSize(L1_SIZE);
        final int sellSize = limitSellBucketSize(L1_SIZE);
        final L1MarketData data = new L1MarketData(buySize, sellSize);
        fillBuys(buySize, data);
        fillSells(sellSize, data);
        fillCode(data);

        data.timestamp = System.currentTimeMillis();

        return data;
    }

    /**
     * 填充股票代码
     *
     * @param data
     */
    void fillCode(L1MarketData data);

    /**
     * 填充买单
     *
     * @param size
     * @param data
     */
    void fillSells(int size, L1MarketData data);

    /**
     * 填充买单
     *
     * @param size
     * @param data
     */
    void fillBuys(int size, L1MarketData data);

    /**
     * 需要知道 买的orderBucket有几组
     * 需要判断 是否需要显示真实的五档 还是更少
     *
     * @param maxSize 整个行情的档位最大值 就是L1MarketData的L1_SIZE
     * @return
     */
    int limitBuyBucketSize(int maxSize);

    /**
     * 需要知道 卖的orderBucket有几组
     * 需要判断 是否需要显示真实的五档 还是更少
     *
     * @param maxSize 整个行情的档位最大值 就是L1MarketData的L1_SIZE
     * @return
     */
    int limitSellBucketSize(int maxSize);
}
