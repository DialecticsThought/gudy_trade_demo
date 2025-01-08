package com.gudy.counter.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/5 20:30
 */
@Component
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO) // 主键自增
    private Integer id;


    @TableField("uid")
    private long uid;

    @TableField("password")
    private String password;
    /**
     * 余额
     */
    @TableField("balance")
    private Long balance;
    /**
     * 账户创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    /**
     * 账户数据更新时间
     */
    @TableField("modify_date")
    private LocalDateTime modifyDate;
    /**
     * 该行数据创建时间
     */
    @TableField("create_date")
    private LocalDateTime createDate;
    /**
     * 该行数据更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


}
