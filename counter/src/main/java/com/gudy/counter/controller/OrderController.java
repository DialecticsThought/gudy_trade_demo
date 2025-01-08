package com.gudy.counter.controller;

import com.gudy.counter.bean.*;
import com.gudy.counter.bean.res.CounterRes;
import com.gudy.counter.cache.StockCache;
import com.gudy.counter.service.UserService;
import com.gudy.counter.service.OrderService;
import com.gudy.counter.service.PosiService;
import com.gudy.counter.service.TradeService;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import static com.gudy.counter.bean.res.CounterRes.FAIL;
import static com.gudy.counter.bean.res.CounterRes.SUCCESS;

@RestController
@RequestMapping("/api")
@Log4j2
public class OrderController {

    @Resource
    private StockCache stockCache;

    @Resource
    private OrderService orderService;

    @Resource
    private TradeService tradeService;

    @Resource
    private PosiService posiService;

    @Resource
    private UserService userService;


    @RequestMapping("/code")
    public CounterRes stockQuery(@RequestParam String key) {
        Collection<StockInfo> stocks = stockCache.getStocks(key);
        return new CounterRes(stocks);
    }

    @RequestMapping("/balance")
    public CounterRes balanceQuery(@RequestParam long uid)
            throws Exception {
        long balance = userService.queryBalance(uid);
        return new CounterRes(balance);
    }

    @RequestMapping("/posi")
    public CounterRes posiQuery(@RequestParam long uid)
            throws Exception {
        List<Posi> postList = posiService.getPosiList(uid);
        return new CounterRes(postList);
    }

    @RequestMapping("/order")
    public CounterRes orderQuery(@RequestParam long uid)
            throws Exception {
        List<Order> orderList = orderService.getOrderList(uid);
        return new CounterRes(orderList);
    }

    @RequestMapping("/trade")
    public CounterRes tradeQuery(@RequestParam long uid) {
        List<Trade> tradeList = tradeService.getTradeList(uid);
        return new CounterRes(tradeList);
    }

    @RequestMapping("/sendorder")
    public CounterRes order(
            @RequestParam int uid,
            @RequestParam short type,
            @RequestParam long timestamp,
            @RequestParam int code,
            @RequestParam byte direction,
            @RequestParam long price,
            @RequestParam long volume,
            @RequestParam byte ordertype
    ) {
        if (orderService.sendOrder(uid, type, timestamp, code, direction, price,
                volume, ordertype)) {
            return new CounterRes(SUCCESS, "save success", null);
        } else {
            return new CounterRes(FAIL, "save failed", null);
        }

    }

    @RequestMapping("/cancelorder")
    public CounterRes cancelOrder(@RequestParam int uid,
                                  @RequestParam int counteroid,
                                  @RequestParam int code) {

        if (orderService.cancelOrder(uid, counteroid, code)) {
            return new CounterRes(SUCCESS, "success", null);
        } else {
            return new CounterRes(FAIL, "failed", null);
        }
    }


}
