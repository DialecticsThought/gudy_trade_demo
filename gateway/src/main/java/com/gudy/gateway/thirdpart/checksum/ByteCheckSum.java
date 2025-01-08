package com.gudy.gateway.thirdpart.checksum;

import org.springframework.stereotype.Component;

@Component
public class ByteCheckSum implements ICheckSum {
    /*
     * [1,2,3,4]
     * sum 目前是 00000000，b 是 1，二进制表示为 00000001。
     * sum = sum ^ 1;
     *       00000000   (sum)
     *     ^ 00000001   (1)
     *     -----------
     *      00000001   (结果)
     * sum 目前是 00000001，b 是 2，二进制表示为 00000010
     *       00000001   (sum)
     *     ^ 00000010   (2)
     *     -----------
     *       00000011   (结果)
     *sum 目前是 00000011，b 是 3，二进制表示为 00000011。
     *        00000011   (sum)
     *      ^ 00000011   (3)
     *      -----------
     *        00000000   (结果)
     * sum 目前是 00000000，b 是 4，二进制表示为 00000100
     *       00000000   (sum)
     *     ^ 00000100   (4)
     *      -----------
     *       00000100   (结果)
     * */
    @Override
    public byte getChecksum(byte[] data) {
        byte sum = 0;
        for (byte b : data) {
            sum ^= b;
        }
        return sum;
    }
}
