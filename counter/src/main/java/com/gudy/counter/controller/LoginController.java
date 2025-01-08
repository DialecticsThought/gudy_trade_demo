package com.gudy.counter.controller;

import com.gudy.counter.bean.User;
import com.gudy.counter.bean.res.CaptchaRes;
import com.gudy.counter.bean.res.CounterRes;
import com.gudy.counter.cache.CacheType;
import com.gudy.counter.cache.RedisCache;
import com.gudy.counter.service.UserService;
import com.gudy.counter.thirdpart.uuid.GudyUuid;
import com.gudy.counter.util.Captcha;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.gudy.counter.bean.res.CounterRes.SUCCESS;


/**
 * @Description
 * @Author veritas
 * @Data 2025/1/5 18:38
 */
@RestController
@RequestMapping("/login")
@Log4j2
public class LoginController {
    @Resource
    private RedisCache redisCache;
    @Resource
    private UserService userService;
    @Resource
    GudyUuid gudyUuid;

    @RequestMapping("/captcha")
    public CounterRes captcha() {
        // 生成验证码 120 pix * 40 pix + 噪点 —+ 线条
        Captcha captcha = new Captcha(120, 40,
                4, 10);
        //2.将验证码<ID,验证码数值>放入缓存
        String uuid = String.valueOf(gudyUuid.getUUID());
        redisCache.cache(uuid, captcha.getCode(),
                CacheType.CAPTCHA);

        //3.使用base64编码图片，并返回给前台
        //uuid,base64
        CaptchaRes res = new CaptchaRes(uuid, captcha.getBase64ByteStr());
        return new CounterRes(res);
    }

    @RequestMapping("/loginfail")
    public CounterRes loginFail() {
        return new CounterRes(CounterRes.RELOGIN, "请重新登陆", null);
    }

    @RequestMapping("/userlogin")
    public CounterRes login(@RequestParam long uid,
                            @RequestParam String password,
                            @RequestParam String captcha,
                            @RequestParam String captchaId) throws Exception {

        User user = userService.login(uid, password,
                captcha, captchaId);

        if (user == null) {
            return new CounterRes(CounterRes.FAIL, "用户名密码/验证码错误，登录失败", null);
        } else {
            return new CounterRes(user);
        }
    }

    //退出登录
    @RequestMapping("/logout")
    public CounterRes logout(@RequestParam String token) {
        userService.logout(token);
        return new CounterRes(SUCCESS, "退出成功", null);
    }

    @RequestMapping("/pwdupdate")
    public CounterRes pwdUpdate(@RequestParam int uid,
                                @RequestParam String oldpwd,
                                @RequestParam String newpwd) {
        Integer res = userService.updatePwd(uid, oldpwd, newpwd);
        if (res == 1) {
            return new CounterRes(CounterRes.SUCCESS, "密码更新成功", null);
        } else {
            return new CounterRes(CounterRes.FAIL, "密码更新失败", null);
        }

    }
}
