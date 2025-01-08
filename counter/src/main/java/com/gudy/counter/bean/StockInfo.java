package com.gudy.counter.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 15:32
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
public class StockInfo {

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
}
