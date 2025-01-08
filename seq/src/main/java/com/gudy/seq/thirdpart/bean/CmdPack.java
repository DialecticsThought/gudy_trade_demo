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

    private long packNo;

    private List<OrderCmd> orderCmds;

}
