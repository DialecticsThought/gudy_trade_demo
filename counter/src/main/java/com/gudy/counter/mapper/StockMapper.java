package com.gudy.counter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gudy.counter.bean.Stock;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:34
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}
