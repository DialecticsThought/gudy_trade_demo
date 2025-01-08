package com.gudy.counter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gudy.counter.bean.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:33
 */
@Mapper
public interface OrderMapper  extends BaseMapper<Order> {

    List<Order> queryOrder(Long uid);
}
