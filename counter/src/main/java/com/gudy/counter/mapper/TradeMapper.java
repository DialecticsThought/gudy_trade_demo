package com.gudy.counter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gudy.counter.bean.Trade;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:34
 */
@Mapper
public interface TradeMapper extends BaseMapper<Trade> {

    List<Trade> queryTrade(Long uid);
}
