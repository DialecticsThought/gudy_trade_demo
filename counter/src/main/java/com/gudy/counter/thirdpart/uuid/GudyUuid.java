package com.gudy.counter.thirdpart.uuid;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GudyUuid {

    @Value("${counter.dataCenterId}")
    private Long dataCenterId;

    @Value("${counter.rackId}")
    private Long workerId;

/*    private static GudyUuid ourInstance = new GudyUuid();

    public static GudyUuid getInstance() {
        return ourInstance;
    }*/

    private GudyUuid() {
    }

    public void init(long centerId, long workerId) {
        idWorker = new SnowflakeIdWorker(workerId, centerId);
    }

    private SnowflakeIdWorker idWorker;

    public long getUUID() {
        return idWorker.nextId();
    }

    @PostConstruct
    public void startup() {
        init(workerId,dataCenterId);
    }

}
