package com.gudy.counter.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description 用于表示股票的基本信息，包括代码、名称、状态等。
 * @Author veritas
 * @Data 2025/1/6 14:13
 */
@Data
@TableName("t_stock") // 指定表名
public class Stock {

    /**
     * 主键 ID，自增字段
     */
    @TableId(type = IdType.AUTO) // 主键自增
    private Integer id;

    /**
     * 股票代码
     */
    private Integer code;
    /**
     * 股票名称
     */
    private String name;

    /**
     * 股票简称
     */
    private String abbrName;

    /**
     * 股票状态：0-停牌，1-退市，2-正常交易
     */
    private Integer status;

    /**
     * 创建时间
     * <p>
     * 默认值为记录插入的时间，自动生成。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>
     * 每次记录更新时，自动刷新。
     */
    private LocalDateTime updateTime;
}
