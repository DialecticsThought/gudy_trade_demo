package com.gudy.counter.cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/5 19:42
 */
@Component

public class RedisCache {
    // 方便静态调用该类的方法
    private static RedisCache redisCache;

    private RedisCache() {}

    @Resource
    private StringRedisTemplate template;

    @Value("${cache-expire.captcha}")
    private int captchaExpireTime;

    @Value("${cache-expire.account}")
    private int accountExpireTime;

    @Value("${cache-expire.order}")
    private int orderExpireTime;

    public static RedisCache getRedisCache() {
        return redisCache;
    }

    public static void setRedisCache(RedisCache redisCache) {
        RedisCache.redisCache = redisCache;
    }

    public StringRedisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(StringRedisTemplate template) {
        this.template = template;
    }

    public int getCaptchaExpireTime() {
        return captchaExpireTime;
    }

    public void setCaptchaExpireTime(int captchaExpireTime) {
        this.captchaExpireTime = captchaExpireTime;
    }

    public int getAccountExpireTime() {
        return accountExpireTime;
    }

    public void setAccountExpireTime(int accountExpireTime) {
        this.accountExpireTime = accountExpireTime;
    }

    public int getOrderExpireTime() {
        return orderExpireTime;
    }

    public void setOrderExpireTime(int orderExpireTime) {
        this.orderExpireTime = orderExpireTime;
    }

    @PostConstruct
    private void init() {
        redisCache = new RedisCache();
        redisCache.setCaptchaExpireTime(captchaExpireTime);
        redisCache.setAccountExpireTime(accountExpireTime);
        redisCache.setOrderExpireTime(orderExpireTime);
    }


    //增加缓存
    public static void cache(String key, String value, CacheType cacheType) {
        int expireTime;
        switch (cacheType) {
            case CacheType.ACCOUNT:
                expireTime = redisCache.getAccountExpireTime();
                break;
            case CacheType.CAPTCHA:
                expireTime = redisCache.getCaptchaExpireTime();
                break;
            case CacheType.ORDER:
            case CacheType.TRADE:
            case CacheType.POSI:
                expireTime = redisCache.getOrderExpireTime();
                break;
            default:
                expireTime = 10;
        }

        redisCache.getTemplate()
                .opsForValue().set(cacheType.type() + key, value
                        , expireTime, TimeUnit.SECONDS);
    }

    //查询缓存
    public static String get(String key, CacheType cacheType) {
        return redisCache.getTemplate()
                .opsForValue().get(cacheType.type() + key);
    }

    //删除缓存
    public static void remove(String key, CacheType cacheType) {
        redisCache.getTemplate()
                .delete(cacheType.type() + key);
    }
}
