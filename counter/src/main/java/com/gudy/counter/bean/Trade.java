package com.gudy.counter.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description 成交的类
 * @Author veritas
 * @Data 2025/1/6 13:32
 */
@Data
@TableName("t_trade") // 对应数据库表名
public class Trade {
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    /**
     * 用户 ID
     */
    private Long uid;

    /**
     * 股票代码
     */
    private Integer code;

    /**
     * 买卖方向（1表示买入，2表示卖出）
     */
    private Integer direction;

    /**
     * 成交价格
     */
    private Long price;

    /**
     * 成交数量
     */
    private Long tradeCount;

    /**
     * 对应的委托单 ID
     */
    private Integer orderId;

    /**
     * 成交日期（格式：YYYYMMDD）
     */
    private LocalDateTime tradeDate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 股票名称（外键关联字段，t_trade 表中不存在）
     */
    @TableField(exist = false) // 指定为非数据库字段
    private String stockName;
}
