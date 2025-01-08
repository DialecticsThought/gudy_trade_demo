package com.gudy.counter.filter;


import com.google.common.collect.Sets;
import com.gudy.counter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * @Description TODO filter 解决跨域 + 校验请求的url合法性 + 校验token
 * @Author veritas
 * @Data 2025/1/5 18:35
 */
@Component
public class SessionCheckFilter implements Filter {
    @Resource
    private UserService userService;
    // TODO 下面的url是不需要身份校验的
    private Set<String> whiteRootPaths = Sets.newHashSet(
            "login", "msgsocket", "test","hello"
    );


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 获取请求
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 虎丘请求的路径
        //http://localhost:8090/login/pwdsetting
        //  /login/pwdsetting
        String path = request.getRequestURI();

        String[] split = path.split("/");

        // 解决ajax 跨域问题
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        response.setHeader("Access-Control-Allow-Origin", "*");

        if (split.length < 2) {

            // 跳转到登录失败的url
            request.getRequestDispatcher("/login/loginfail")
                    .forward(servletRequest, servletResponse);
        } else {
            if (!whiteRootPaths.contains(split[1])) {
                //不在白名单 得到请求的token字段 验证token
                if (userService.accountExistInCache(request.getParameter("token"))) {
                    // 放晴
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    // 跳转到登录失败的url
                    request.getRequestDispatcher(
                            "/login/loginfail"
                    ).forward(servletRequest, servletResponse);
                }
            } else {
                //在白名单 放行
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
    }
}
