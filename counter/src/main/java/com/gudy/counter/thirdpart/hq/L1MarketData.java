package com.gudy.counter.thirdpart.hq;

import lombok.ToString;

import java.io.Serializable;

/**
 * 一级市场的五档行情
 */
@ToString
public class L1MarketData implements Serializable {
    /**
     * 5档
     */
    @ToString.Exclude
    public static final int L1_SIZE = 5;
    /**
     * 股票代码，用于标识股票。比如某个特定的股票，可能是类似 "600519"（贵州茅台的股票代码）这样的数字或字符串
     */
    public int code;
    /**
     * 最新成交价格。这个价格是最后一次成交的价格，它反映了市场上最新交易的价格
     */
    public long newPrice;

    /**
     * 买卖实际档位数量
     * 分别表示买单和卖单在不同价格档位上的数量。
     * 例如，有的行情数据可能只有三档有效买卖，而不是五档，因此这些值用于存储有效买卖的档数
     */
    @ToString.Exclude
    public transient int buySize;
    /**
     * 买卖实际档位数量
     * 分别表示买单和卖单在不同价格档位上的数量。
     * 例如，有的行情数据可能只有三档有效买卖，而不是五档，因此这些值用于存储有效买卖的档数
     */
    @ToString.Exclude
    public transient int sellSize;
    /**
     * 买一到买五价格（Buy Prices）：即买盘的五个档位，显示当前市场上五个不同价格的买单
     */
    public long[] buyPrices;
    /**
     * 买一到买五数量（Buy Volumes）：与每个买价相对应的买单量
     */
    public long[] buyVolumes;
    /**
     * 卖一到卖五价格（Sell Prices）：即卖盘的五个档位，显示当前市场上五个不同价格的卖单。
     */
    public long[] sellPrices;
    /**
     * 卖一到卖五数量（Sell Volumes）：与每个卖价相对应的卖单量
     */
    public long[] sellVolumes;

    public long timestamp;

    public L1MarketData(long[] buyPrices, long[] buyVolumes,
                        long[] sellPrices, long[] sellVolumes) {
        this.buyPrices = buyPrices;
        this.buyVolumes = buyVolumes;
        this.sellPrices = sellPrices;
        this.sellVolumes = sellVolumes;

        this.buySize = buyPrices.length;
        this.sellSize = sellPrices.length;
    }

    public L1MarketData(int buySize, int sellSize) {
        this.buyPrices = new long[buySize];
        this.buyVolumes = new long[buySize];
        this.sellPrices = new long[sellSize];
        this.sellVolumes = new long[sellSize];
    }
}
