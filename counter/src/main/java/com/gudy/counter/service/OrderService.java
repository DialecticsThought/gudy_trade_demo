package com.gudy.counter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gudy.counter.bean.Order;
import com.gudy.counter.thirdpart.order.OrderCmd;
import com.gudy.counter.thirdpart.order.OrderStatus;

import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:35
 */
public interface OrderService extends IService<Order> {
    List<Order> getOrderList(long uid);

    void updateOrder(long uid, int oid, OrderStatus status);

    int saveOrder(OrderCmd orderCmd);

    boolean sendOrder(long uid, short type, long timestamp, int code,
                      byte direction, long price, long volume, int ordertype);

    //boolean cancelOrder(int uid, int counteroid, int code);
    boolean cancelOrder(int uid, int counteroid, int code);
}
