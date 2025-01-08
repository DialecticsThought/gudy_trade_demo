package com.gudy.gateway.thirdpart.checksum;

/*
* 校验和检查器
* */
public interface ICheckSum {

    byte getChecksum(byte[] data);

}
