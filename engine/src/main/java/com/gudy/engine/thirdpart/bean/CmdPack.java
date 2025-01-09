package com.gudy.engine.thirdpart.bean;

import com.gudy.engine.thirdpart.order.OrderCmd;
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
