package com.gudy.engine.thirdpart.hq;

import lombok.ToString;

import java.io.Serializable;

/**
 * 一档行情
 *
 * 档位   sellprices  sellVolumes
 * 卖五   46.93        5
 * 卖四   46.92        10
 * 卖三   46.91        296
 * 卖二   46.90        35
 * 卖一   46.88        601
 *
 * 档位   buyprices  buyVolumes
 * 买一   46.87        30
 * 买二   46.86        51
 * 买三   46.85        10
 * 买四   46.83        16
 * 买五   46.82        52
 */
@ToString
public class L1MarketData implements Serializable {
    /**
     * 5档
     */
    @ToString.Exclude
    public static final int L1_SIZE = 5;
    /**
     * 股票代码
     */
    public int code;
    /**
     * 价格
     */
    public long newPrice;

    /**
     * 买单的实际档位数量
     */
    @ToString.Exclude
    public transient int buySize;
    /**
     * 卖单的实际档位数量
     */
    @ToString.Exclude
    public transient int sellSize;
    /**
     * 每一个档位的价格
     */
    public long[] buyPrices;
    /**
     * 每一个档位的价量
     */
    public long[] buyVolumes;
    /**
     * 每一个档位的价格
     */
    public long[] sellPrices;
    /**
     * 每一个档位的价量
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
