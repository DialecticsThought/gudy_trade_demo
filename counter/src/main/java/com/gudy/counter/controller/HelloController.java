package com.gudy.counter.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/7 21:54
 */
@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        return "hello, world";
    }
}
