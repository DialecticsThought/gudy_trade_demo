package com.gudy.counter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gudy.counter.bean.Member;

import com.gudy.counter.mapper.MemberMapper;

import com.gudy.counter.service.MemberService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Description
 * @Author veritas
 * @Data 2025/1/10 12:16
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Override
    public short[] queryAllMemberIds() throws Exception {
        // 创建 QueryWrapper 以生成 SQL 查询条件
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").eq("status", 1);  // 查询状态为 1 的会员 ID

        List<Member> members = list(queryWrapper);  // 执行查询
        if (members == null || members.isEmpty()) {
            throw new Exception("member empty");
        }

        short[] memberIds = new short[members.size()];
        int i = 0;
        for (Member member : members) {
            memberIds[i] = member.getId();
            i++;
        }
        return memberIds;
    }
}
