package com.gudy.counter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gudy.counter.bean.User;
import com.gudy.counter.bean.res.AccountRes;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/5 20:35
 */
public interface UserService extends IService<User> {

    Long queryBalance(long uid);

    User findAccountByUidAndPassword(long uid, String password);

    // 使用 UpdateWrapper 更新
    int updateLoginTime(long uid);


    int updatePwd(long uid, String oldPwd, String newPwd);

    AccountRes convertToAccountRes(User user);

    User login(long uid, String password,
               String captcha, String captchaId) throws Exception;

    boolean accountExistInCache(String token);

    //清除缓存登录信息
    boolean logout(String token);

    Integer addBalance(Long uid, Long balance);

    Integer minusBalance(Long uid, Long balance);
}
