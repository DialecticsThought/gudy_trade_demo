package com.gudy.counter.util;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/7 19:11
 */
public class IDConverter {

    public static long combineInt2Long(int high, int low) {
        /*
         * high 先 向左无符号转移32位
         * 0xFFFFFFFF00000000L 的意思是确保 低位全是0
         * 再和低位 做或运算
         * */
        return ((long) high << 32 & 0xFFFFFFFF00000000L) | ((long) low & 0xFFFFFFFFL);
    }

    public static int[] seperateLong2Int(Long val) {
        int[] result = new int[2];
        // 把高32位全部清零 通过与运算
        result[0] = (int) (0xFFFFFFFFL & val);// 得到低位
        // 把低32位清零之后的数字 向右移动32位 就是原有Long类型的高32位
        result[1] = (int) ((0xFFFFFFFF00000000L & val) >> 32);// 得到高位

        return result;
    }

}
