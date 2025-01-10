package com.gudy.counter.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/10 12:14
 */
@TableName("t_member")
@Data
public class Member {

    private Short id;

    private Integer status;
}
