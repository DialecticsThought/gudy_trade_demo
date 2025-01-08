package com.gudy.counter.bean.res;

import lombok.Data;

/**
 * @Description 通用返回格式
 * @Author veritas
 * @Data 2025/1/5 18:39
 */
@Data
public class CounterRes {

    public static final int SUCCESS = 0;
    public static final int RELOGIN = 1;
    public static final int FAIL = 2;

    private int code;

    private String message;

    private Object data;

    public CounterRes() {
    }

    public CounterRes(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public CounterRes(Object data) {
        this(0, "", data);
    }
}
