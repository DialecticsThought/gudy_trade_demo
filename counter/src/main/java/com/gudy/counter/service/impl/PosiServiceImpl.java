package com.gudy.counter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.gudy.counter.bean.Posi;
import com.gudy.counter.cache.CacheType;
import com.gudy.counter.cache.RedisCache;
import com.gudy.counter.mapper.PosiMapper;
import com.gudy.counter.service.PosiService;
import com.gudy.counter.util.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:36
 */
@Service
public class PosiServiceImpl extends ServiceImpl<PosiMapper, Posi> implements PosiService {
    @Resource
    private PosiMapper posiMapper;

    List<Posi> queryPosiByUid(Long uid) {
        return posiMapper.queryPosiByUid(uid);
    }

    Posi queryPosiByUidAndCode(Long uid, Integer code) {
        return posiMapper.queryPosiByUidAndCode(uid, code);
    }

    @Override
    public List<Posi> getPosiList(long uid) {
        //查缓存
        String suid = Long.toString(uid);
        String posiS = RedisCache.get(suid, CacheType.POSI);
        if (StringUtils.isEmpty(posiS)) {
            //未查到 查库
            List<Posi> tmp = queryPosiByUid(uid);
            List<Posi> result =
                    CollectionUtils.isEmpty(tmp) ? Lists.newArrayList()
                            : tmp;
            //更新缓存
            RedisCache.cache(suid, JsonUtil.toJson(result), CacheType.POSI);
            return result;
        } else {
            //查到 命中缓存
            return JsonUtil.fromJsonArr(posiS, Posi.class);
        }
    }

    @Override
    public List<Posi> selectList(Long uid) {
        QueryWrapper<Posi> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UId", uid);

        return posiMapper.selectList(queryWrapper);
    }

    @Override
    public Posi getPosi(long uid, int code) {
        return queryPosiByUidAndCode(uid, code);
    }

    @Override
    public void addPosi(long uid, int code, long volume, long price) {
        //持仓是否存在
        Posi Posi = getPosi(uid, code);
        if (Posi == null) {// 如果 用户没有该股票的持仓
            //新增一条持仓
            insertPosi(uid, code, volume, price);
        } else {// 如果 用户有该股票的持仓
            //修改持仓
            Posi.setCount(Posi.getCount() + volume);//持仓数量 = 原有持仓数 + 新增
            Posi.setCost(Posi.getCost() + price * volume);// 原有股票持有成本 + 新买入的股票持有成本
            // 如果修改后的持仓数 = 0 就说明没有持仓  删去
            // 但是实际上 每天收盘 各个机构在清算的时候才会之执行delete
            // 这里只做修改
            updatePosi(Posi);
        }
    }

    @Override
    public void minusPosi(long uid, int code, long volume, long price) {
        addPosi(uid, code, -volume, price);
    }

    private int updatePosi(Posi Posi) {
        UpdateWrapper<com.gudy.counter.bean.Posi> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", Posi.getUid());
        updateWrapper.eq("code", Posi.getCode());
        updateWrapper.set("count", Posi.getCount());
        updateWrapper.set("cost", Posi.getCost());

        return posiMapper.update(updateWrapper);
    }

    @Override
    public void insertPosi(long uid, int code, long volume, long price) {
        Posi posi = new Posi();
        posi.setUid(uid);
        posi.setCode(code);
        posi.setCount(volume);
        posi.setCost(volume * price);
        posi.setCreateTime(LocalDateTime.now());
        posi.setUpdateTime(LocalDateTime.now());
        posiMapper.insert(posi);
    }
}
