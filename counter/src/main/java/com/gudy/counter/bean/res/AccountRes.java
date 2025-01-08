package com.gudy.counter.bean.res;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class AccountRes {

    private int id;

    private long uid;
    /**
     * 和 modifyDate对应
     */
    private String lastLoginDate;
    /**
     * 和 modifyTime对应
     */
    private String updateTime;

    private String token;

    public AccountRes(int id, long uid, LocalDateTime modifyDate, LocalDateTime modifyTime) {
        this.id = id;
        this.uid = uid;
        this.lastLoginDate = modifyDate.toString();
        this.updateTime = modifyTime.toString();
    }
}
