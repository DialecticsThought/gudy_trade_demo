package com.gudy.seq.thirdpart.bean;

import com.gudy.seq.thirdpart.order.OrderCmd;
import lombok.*;


import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CmdPack implements Serializable {
    /*
    * 包的编号 查看乱序和丢包
    * */
    private long packNo;
    /*
    * 委托数据
    * */
    private List<OrderCmd> orderCmds;

}
