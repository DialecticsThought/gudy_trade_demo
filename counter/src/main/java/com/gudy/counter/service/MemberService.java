package com.gudy.counter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gudy.counter.bean.Member;
import com.gudy.counter.bean.User;
import com.gudy.counter.bean.res.AccountRes;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/5 20:35
 */
public interface MemberService extends IService<Member> {

    short[] queryAllMemberIds() throws Exception;
}
