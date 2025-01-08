package com.gudy.counter.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gudy.counter.bean.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/5 20:34
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 你可以在这里添加自定义的 SQL 查询方法
}
