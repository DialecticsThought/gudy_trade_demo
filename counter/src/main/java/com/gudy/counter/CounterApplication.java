package com.gudy.counter;

import com.gudy.counter.config.CounterConfig;
import com.gudy.counter.thirdpart.uuid.GudyUuid;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gudy.counter.mapper")
public class CounterApplication {
/*    @Resource
    private CounterConfig config;

    @PostConstruct
    private void init() {
        // 初始化UUID的相关参数
        GudyUuid.getInstance().init(config.getDataCenterId(), config.getWorkerId());
    }*/

    public static void main(String[] args) {
        SpringApplication.run(CounterApplication.class, args);
    }

}
