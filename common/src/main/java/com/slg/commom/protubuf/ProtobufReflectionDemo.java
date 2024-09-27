package com.slg.commom.protubuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import java.lang.reflect.Method;

public class ProtobufReflectionDemo {

    @SuppressWarnings("unchecked")
    public static <T extends Message> T parseFrom(String className, byte[] data) throws Exception {
        // 加载Protobuf类
        Class<T> clazz = (Class<T>) Class.forName(className);

        // 获取parseFrom方法
        Method parseFromMethod = clazz.getMethod("parseFrom", ByteString.class);

        // 使用ByteString封装数据
        ByteString byteString = ByteString.copyFrom(data);

        // 调用parseFrom方法
        return (T) parseFromMethod.invoke(null, byteString);
    }

//    public static void main(String[] args) throws Exception {
//        // 假设你有一个名为"MyProtoMessage"的Protobuf类
//        String className = "com.example.MyProtoMessage";
//        byte[] protoData = ...; // 这里应该是你的Protobuf序列化后的数据
//
//        MyProtoMessage message = parseFrom(className, protoData);
//        // 接下来可以对message进行操作
//    }
}
