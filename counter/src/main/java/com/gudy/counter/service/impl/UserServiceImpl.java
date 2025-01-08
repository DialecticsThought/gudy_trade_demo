package com.gudy.counter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gudy.counter.bean.User;
import com.gudy.counter.bean.res.AccountRes;
import com.gudy.counter.cache.CacheType;
import com.gudy.counter.cache.RedisCache;
import com.gudy.counter.mapper.UserMapper;
import com.gudy.counter.service.UserService;
import com.gudy.counter.thirdpart.uuid.GudyUuid;
import com.gudy.counter.util.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/5 20:36
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource

    private GudyUuid gudyUuid;

    @Override
    public Long queryBalance(long uid) {
        QueryWrapper<User> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("uid", uid);

        User user = userMapper.selectOne(orderQueryWrapper);

        return user.getBalance();
    }

    @Override
    public User findAccountByUidAndPassword(long uid, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("password", password);
        return userMapper.selectOne(queryWrapper); // getOne 方法用于查询单条数据
    }

    /**
     * 使用 UpdateWrapper 更新
     *
     * @param uid
     * @return
     */
    @Override
    public int updateLoginTime(long uid) {
        // 创建 UpdateWrapper
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid); // 条件：uid = #{uid}
        updateWrapper.set("modify_date", LocalDateTime.now()); // 要更新的字段：lastLoginTime
        updateWrapper.set("update_time", LocalDateTime.now()); // 要更新的字段：数据库改行字段更新时间
        // 执行更新
        return userMapper.update(null, updateWrapper);
    }

    @Override
    public int updatePwd(long uid, String oldPwd, String newPwd) {
        // 创建 UpdateWrapper
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", oldPwd); // 条件：uid = #{uid}
        updateWrapper.set("password", newPwd);
        updateWrapper.set("update_time", LocalDateTime.now()); // 要更新的字段：数据库改行字段更新时间
        // 执行更新
        return userMapper.update(null, updateWrapper);
    }

    @Override
    public AccountRes convertToAccountRes(User user) {
        return new AccountRes(user.getId(), user.getUid(), user.getModifyDate(), user.getUpdateTime());
    }

    /*
     * TODO
     *  请求 -> filter -> controller
     *  filter 解决跨域 + 校验请求的url合法性 + 校验token
     * */
    @Override
    public User login(long uid, String password,
                      String captcha, String captchaId) throws Exception {
        //1.入参的合法性校验
        if (StringUtils.isAnyBlank(password, captcha,
                captchaId)) {
            return null;
        }
        //2.校验缓存验证码
        String captchaCache =
                RedisCache.get(captchaId, CacheType.CAPTCHA);
        if (StringUtils.isEmpty(captchaCache)) {
            return null;
        } else if (!StringUtils.equalsIgnoreCase(captcha, captchaCache)) {
            return null;
        }
        RedisCache.remove(captchaId, CacheType.CAPTCHA);
        //3.比对数据库用户名和密码
        User user = findAccountByUidAndPassword(uid, password);
        if (user == null) {
            return null;
        } else {
            AccountRes accountRes = convertToAccountRes(user);
            //增加唯一ID作为身份标志
            accountRes.setToken(String.valueOf(
                    gudyUuid.getUUID()
            ));

            //存入缓存
            RedisCache.cache(String.valueOf(
                            accountRes.getToken()), JsonUtil.toJson(user),
                    CacheType.ACCOUNT
            );
            updateLoginTime(uid);

            return user;
        }
    }

    @Override
    public boolean accountExistInCache(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        //从缓存获取数据
        String acc = RedisCache.get(token, CacheType.ACCOUNT);
        if (acc != null) {
            RedisCache.cache(token, acc, CacheType.ACCOUNT);
            return true;
        } else {
            return false;
        }
    }

    /**
     * TODO 登出 == 清除缓存登录信息
     *
     * @param token
     * @return
     */
    @Override
    public boolean logout(String token) {
        RedisCache.remove(token, CacheType.ACCOUNT);
        return true;
    }

    @Override
    public Integer addBalance(Long uid, Long balance) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        updateWrapper.setSql("balance = balance + " + balance);
        updateWrapper.eq("uid", uid);

        return userMapper.update(updateWrapper);
    }

    @Override
    public Integer minusBalance(Long uid, Long balance) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        updateWrapper.setSql("balance = balance - " + balance);
        updateWrapper.eq("uid", uid);

        return userMapper.update(updateWrapper);
    }
}
