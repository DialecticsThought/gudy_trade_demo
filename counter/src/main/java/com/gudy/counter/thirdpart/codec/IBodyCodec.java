package com.gudy.counter.thirdpart.codec;

public interface IBodyCodec {

    //1.obj --> byte[]  把java对象编程字节数组
    <T> byte[] serialize(T obj) throws Exception;


    //2.byte[] --> obj 把字节数组编程java对象
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception;


}
