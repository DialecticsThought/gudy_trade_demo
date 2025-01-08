package com.gudy.counter.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description 委托的类
 * @Author veritas
 * @Data 2025/1/6 13:31
 */
@Data
@TableName("t_order") // 对应数据库表名
public class Order {
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO) // 主键自增
    private Integer id;

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
     * 委托类型
     */
    private Integer type;

    /**
     * 委托价格
     */
    private Long price;

    /**
     * 委托数量
     */
    private Long orderCount;

    /**
     * 委托状态（如：0-待处理，1-已完成）
     */
    private Integer status;

    /**
     * 委托日期（格式：YYYYMMDD）
     */
    private LocalDateTime orderDate;

    /**
     * 创建时间（格式：YYYYMMDD）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 股票名称（外键关联字段，t_order 表中不存在）
     */
    @TableField(exist = false) // 指定为非数据库字段
    private String stockName;
}
