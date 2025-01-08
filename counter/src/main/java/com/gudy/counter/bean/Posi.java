package com.gudy.counter.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description  持仓的类
 * @Author veritas
 * @Data 2025/1/6 13:33
 */
@Data
@TableName("t_posi") // 对应数据库表名
public class Posi {
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
     * 股票持有成本（股票持有数量 * 当时买入价格）
     */
    private Long cost;

    /**
     * 股票持有数量
     */
    private Long count;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 股票名称（外键关联字段，t_posi 表中不存在）
     */
    @TableField(exist = false) // 指定为非数据库字段
    private String stockName;
}
