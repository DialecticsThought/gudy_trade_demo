package com.gudy.seq.Bean;

import com.alipay.sofa.jraft.rhea.LeaderStateListener;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/8 18:26
 */
@Data
public class Node {
    /*
     * k v 数据库的初始化操作的类
     * 这个类是外部创建node对象的时候传入的
     * */
    private RheaKVStoreOptions options;
    /*
     * k v 数据库
     * */
    private RheaKVStore rheaKVStore;
    /*
     * 判断节点是否是leader
     * */
    private final AtomicLong leaderTerm = new AtomicLong(-1);

    public Node(RheaKVStoreOptions options) {
        this.options = options;
    }

    public boolean isLeader() {
        return leaderTerm.get() > 0;
    }

    /*
     * node 节点的停止
     * */
    public void stop() {
        rheaKVStore.shutdown();
    }

    /*
     * node 节点的启动
     * */
    public void start() {
        // 初始化
        rheaKVStore = new DefaultRheaKVStore();

        rheaKVStore.init(this.options);
        // 监听的节点状态
        rheaKVStore.addLeaderStateListener(-1, new LeaderStateListener() {
            /*
             * 执行这个方法说明了该节点是leader
             * */
            @Override
            public void onLeaderStart(long newTerm) {
                leaderTerm.set(newTerm);
            }

            /*
             * 执行 这个方法说明了该节点不是leader
             * */
            @Override
            public void onLeaderStop(long oldTerm) {
                leaderTerm.set(-1);
            }
        });
    }
}
