package com.gudy.engine.bean.handler;

import com.gudy.engine.bean.command.CmdResultCode;
import com.gudy.engine.bean.command.RingBufferCmd;
import com.gudy.engine.bean.orderbook.IOrderBook;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;


@Data
@AllArgsConstructor
public class StockMatchHandler extends BaseHandler {
    /**
     * 给每一个股票代码都要一个orderBook
     */
    private IntObjectHashMap<IOrderBook> orderBookMap;

    @Override
    public void onEvent(RingBufferCmd cmd, long sequence, boolean endOfBatch) throws Exception {
        // 这个判断是 是否通过了前置风控的校验
        if (cmd.resultCode.getCode() < 0) {
            return;//风控未通过
        }

        cmd.resultCode = processCmd(cmd);
    }

    private CmdResultCode processCmd(RingBufferCmd cmd) {
        switch (cmd.command) {
            case NEW_ORDER:// 新订单
                return orderBookMap.get(cmd.code).newOrder(cmd);
            case CANCEL_ORDER:// 撤单
                return orderBookMap.get(cmd.code).cancelOrder(cmd);
            case HQ_PUB:// 发布行情
                // 遍历所有orderBook 把每一个股票的行情快照 丢到外面
                orderBookMap.forEachKeyValue((code, orderBook) ->
                        cmd.marketDataMap.put(code, orderBook.getL1MarketDataSnapshot())
                );
            default:
                return CmdResultCode.SUCCESS;
        }
    }
}
