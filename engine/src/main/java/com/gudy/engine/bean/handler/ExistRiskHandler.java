package com.gudy.engine.bean.handler;

import com.gudy.engine.bean.command.CmdResultCode;
import com.gudy.engine.bean.command.RbCmd;
import com.gudy.engine.thirdpart.order.CmdType;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

/**
 * @Description TODO 前置风控处理器
 * @Author veritas
 * @Data 2025/1/10 13:34
 */
@Log4j2
public class ExistRiskHandler extends BaseHandler {
    /**
     * 用户id的集合
     */
    private MutableLongSet uidSet;
    /**
     * 股票代码集合
     */
    private MutableIntSet codeSet;

    /**
     * 1.判断用户是否存在
     * 2.股票是否合法
     *
     * @param rbCmd
     * @param l
     * @param b
     * @throws Exception
     */
    @Override
    public void onEvent(RbCmd rbCmd, long l, boolean b) throws Exception {
        // 只处理 三种消息 发布航行event 新的委托event 撤单event
        // 实际情况有很多event

        if (rbCmd.command == CmdType.HQ_PUB) {// 行情发布 需要前置风控判断
            return;
        }
        if (rbCmd.command == CmdType.NEW_ORDER || rbCmd.command == CmdType.CANCEL_ORDER) {
            // 判断用户是否存在
            if (!uidSet.contains(rbCmd.uid)) {
                log.error("illegal uid {} exist", rbCmd.uid);
                // 修改这个委托的标记 后续handler不会再处理这类委托
                rbCmd.resultCode = CmdResultCode.RISK_INVALID_USER;
            }
            // 股票代码是否合法
            if (!codeSet.contains(rbCmd.code)) {
                log.error("illegal uid {} exist", rbCmd.uid);
                // 修改这个委托的标记 后续handler不会再处理这类委托
                rbCmd.resultCode = CmdResultCode.RISK_INVALID_CODE;
            }
        }
    }
}
