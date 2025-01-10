package com.gudy.counter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gudy.counter.bean.Stock;
import com.gudy.counter.mapper.StockMapper;
import com.gudy.counter.service.StockService;
import jakarta.annotation.Resource;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:36
 */
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    @Resource
    StockMapper stockMapper;

    /**
     * 给engine模块使用
     * @return
     * @throws Exception
     */
    @Override
    public IntHashSet queryAllStockCode() throws Exception {
        // 创建 QueryWrapper 以生成 SQL 查询条件
        QueryWrapper<Stock> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("code").eq("status", 1);  // 查询状态为 1 的股票代码

        List<Stock> stocks = list(queryWrapper);  // 执行查询
        if (stocks == null || stocks.isEmpty()) {
            throw new Exception("stock empty");
        }

        IntHashSet codes = new IntHashSet();
        for (Stock stock : stocks) {
            codes.add(stock.getCode());
        }
        return codes;
    }

    @Override
    public List<Stock> queryAllStock() {
        return stockMapper.selectList(null);
    }

    @Override
    public List<Map<String, Object>> convertStockToMap(List<Stock> stockList) {

        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Class<?> clazz = Class.forName("com.gudy.counter.bean.Stock");
            Field[] declaredFields = clazz.getDeclaredFields();
            ArrayList<String> fieldNames = new ArrayList<>();
            for (Field field : declaredFields) {
                fieldNames.add(field.getName());
            }
            for (Stock stock : stockList) {
                Map<String, Object> hashMap = new HashMap<>();

                for (String fieldName : fieldNames) {
                    // 通过反射获取字段值
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true); // 允许访问私有字段
                    Object value = field.get(stock); // 获取字段值
                    hashMap.put(fieldName, value);   // 将字段名和字段值存入 Map
                }

                result.add(hashMap);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
