package com.gudy.counter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gudy.counter.bean.Posi;

import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/6 13:35
 */
public interface PosiService extends IService<Posi> {
    List<Posi> getPosiList(long uid);

    List<Posi> selectList(Long uid);

    Posi getPosi(long uid, int code);

    void addPosi(long uid, int code, long volume, long price);

    void minusPosi(long uid, int code, long volume, long price);

    void insertPosi(long uid, int code, long volume, long price);
}
