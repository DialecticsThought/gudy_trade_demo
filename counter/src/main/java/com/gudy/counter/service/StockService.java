package com.gudy.counter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gudy.counter.bean.Stock;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:35
 */
public interface StockService extends IService<Stock> {
    IntHashSet queryAllStockCode() throws Exception;

    List<Stock> queryAllStock();

    List<Map<String, Object>> convertStockToMap(List<Stock> stockList);
}
