package com.gudy.counter.consumer;

import com.gudy.counter.config.CounterConfig;
import com.gudy.counter.thirdpart.hq.L1MarketData;
import com.gudy.counter.util.JsonUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.gudy.counter.config.WebSocketConfig.L1_MARKET_DATA_PREFIX;
import static com.gudy.counter.consumer.MQTTBusConsumer.INNER_MARKET_DATA_CACHE_ADDR;


/**
 * 这个行情处理器
 * 它的作用:  接受磋合核心定时发过来的那个五档行情, 然后缓存下来
 * 方便给委托中端提供查询就可以了。
 */
@Log4j2
@Component
public class MarketDataConsumer {

    @Autowired
    private CounterConfig config;

    /**
     * 缓存所有磋合核心发过来的五档行情数据
     *
     * <股票code,对应最新的五档行情>
     */
    private IntObjectHashMap<L1MarketData> l1Cache = new IntObjectHashMap<>();

    /**
     * 它的初始化就是要从内部的这个vertx的消息总线上订阅行情数据。
     * 地址就是在刚刚的那个MQTTBusConsumer数据处理器当中声明的
     * 对于这部分数据的处理逻辑会放在一个handler当中
     */
    @PostConstruct
    private void init() {
        EventBus eventBus = config.getVertx().eventBus();

        //处理撮合核心发过来的行情的处理器
        // 这个处理器只能处理自己jvm里面的这个消息总线
        eventBus.consumer(INNER_MARKET_DATA_CACHE_ADDR)
                .handler(buffer -> {
                    Buffer body = (Buffer) buffer.body();
                    if (body.length() == 0) {
                        return;
                    }

                    L1MarketData[] marketData = null;
                    try {
                        // 解码 得到 五档行情
                        marketData = config.getBodyCodec().deserialize(body.getBytes(), L1MarketData[].class);
                    } catch (Exception e) {
                        log.error(e);
                    }

                    if (ArrayUtils.isEmpty(marketData)) {
                        return;
                    }
                    // 遍历每一个五档行情数组的数据
                    // 根据每一个五档行情的code,放到对应的map的位置里面去。
                    for (L1MarketData md : marketData) {
                        L1MarketData l1MarketData = l1Cache.get(md.code);
                        // 有可能这笔来的数据,它是旧的
                        // 如果是旧的,那么就要把这笔数据给它过去掉
                        // 怎么判断这笔数据是旧的呢?
                        // 对于已经存在的五档行情 它的时间戳, 以新来的这笔时间戳要大, 那么就说明新来的这笔,它是旧的
                        if (l1MarketData == null || l1MarketData.timestamp < md.timestamp) {
                            l1Cache.put(md.code, md);
                        } else {
                            log.error("l1MarketData is null or l1MarketData.timestamp < md.timestamp");
                        }
                    }

                });
        // 委托中端的行情请求的处理器
        // 这个处理器 要处理websocket的vertx上面的消息的
        // vertx给我们提供了一个很好的特性。
        // 只要创建这两种总线的vertx是一个, 那么他们的消息就是可以共用的。
        // 到底这个总线是jvm内部的, 还是一个基于 websocket的
        // 对于开发者来说,它是透明的。vertx对它做了很好的封装。
        // 只要你是同一个vertx的对象,
        // 产生的这个eventbus,它对于同一个jvm,或者多个jvm,或者网络环境, 都是同用的。

        // 行情这部分数据,是委托终端主动发送请求的。
        // 而且这个请求是按照一个固定频率,往柜台这边来丢的。
        // 所以我们这里的这个consumer要处理的,就是委托终端主动发过来的请求。
        // 请求的数据,是放在它的头里面的。
        eventBus.consumer(L1_MARKET_DATA_PREFIX)
                .handler(h -> {
                    // 通过这个code,
                    // 我们就能拿到委托终端所希望获取哪只股票的这个五档行情。
                    // 有了这个code之后,
                    // 我们就直接去行情的那个缓存里面,把数据拿出来,
                    // 然后回给委托终端就可以了。
                    int code = Integer.parseInt(h.headers().get("code"));
                    L1MarketData data = l1Cache.get(code);
                    // 因为下游的委托中端,
                    //它本质上是一个网页,
                    //所以我们这里回数据,
                    //直接回一个json格式的数据。
                    h.reply(JsonUtil.toJson(data));
                });
    }

}
