package com.slg.module.protubuf;

import com.google.protobuf.*;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BufferSerializableUtilTTT {
    private static final Map<Class<? extends MessageLite>, Parser<? extends MessageLite>> map = new ConcurrentHashMap<>();
    Map<Integer, Descriptors.Descriptor> descriptorMap=new ConcurrentHashMap<>();
    DynamicMessage dynamicMessage;
    private static <M extends MessageLite> Parser<M> doParser(Class<M> type) {
        @SuppressWarnings("unchecked")
        Parser<M> parser = (Parser<M>) map.get(type);
        if (parser != null) {
            return parser;
        }
        AbstractParser<M> abstractParser = new AbstractParser<M>() {
            @Override
            public M parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                try {
                    // 假设M有一个合适的构造函数
                    Constructor<M> c = type.getDeclaredConstructor(CodedInputStream.class, ExtensionRegistryLite.class);
                    c.setAccessible(true);
                    return c.newInstance(input, extensionRegistry);
                } catch (Exception e) {
                    throw new InvalidProtocolBufferException("Failed to instantiate parser for type " + type.getName(), e);
                }
            }
        };
        map.put(type, abstractParser);
        return abstractParser;
    }
    public static <M extends MessageLite> M parser(Class<M> type,byte[] buff) throws InvalidProtocolBufferException {
        com.google.protobuf.Parser<M> mParser = doParser(type);
        return mParser.parseFrom(buff);
    }
    public static byte[] toByteArray(com.google.protobuf.Message.Builder builder){
        return builder.build().toByteArray();
    }

//        public static void main(String[] args) throws InvalidProtocolBufferException {
//            Class<MSG.LoginRequest> loginRequestClass = MSG.LoginRequest.class;
//
//            MSG.LoginRequest.Builder builder = MSG.LoginRequest.newBuilder();
//
//            MSG.LoginRequest.Builder builder2 = MSG.LoginRequest.newBuilder()
//                    .setUsername("aaa")
//                    .setPassword("123123");
//
////            MSG.Request.Builder builder1 = MSG.Request.newBuilder()
////                    .setLogin(builder2);
//
//        byte[] bytes = toByteArray(builder2);
//            MSG.LoginRequest parser = parser(MSG.LoginRequest.class, bytes);
//            System.out.println(parser);
//    }
}
