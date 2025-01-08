package com.gudy.gateway.bean;

import com.google.common.collect.Lists;
import com.gudy.gateway.thirdpart.order.OrderCmd;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Description 网关作为中介，传递柜台的委托和其他信息
 * 所以网关需要一个缓存，把委托信息缓存下来，提供给排队机或者其他模块
 * 写缓存的操作一定是异步的，不能干扰到tcp的连接的处理
 * TODO 这个类就是缓存类
 * @Author veritas
 * @Data 2025/1/7 17:46
 */
@Component
public class OrderCmdContainer {
    /*
     * 缓存的核心
     * */
    private final BlockingQueue<OrderCmd> blockingQueue = new LinkedBlockingDeque<>();

    public boolean cache(OrderCmd cmd) {
        return blockingQueue.offer(cmd);
    }

    public List<OrderCmd> getAll() {
        ArrayList<OrderCmd> orderCmds = Lists.newArrayList();
        /*
         * drainTo 不想pull是非阻塞的，并且一次性把所有的委托 拿出 并清空 阻塞队列
         * */
        int count = blockingQueue.drainTo(orderCmds);
        /*
         * 因为 非阻塞，可能取不到数据 需要判断
         * */
        if (count == 0) {
            return null;
        } else {
            return orderCmds;
        }
    }

    public Integer size() {
        return blockingQueue.size();
    }
}
