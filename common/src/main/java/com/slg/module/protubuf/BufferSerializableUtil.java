//package com.slg.commom.protubuf;
//
//import com.google.protobuf.*;
//import java.lang.reflect.Constructor;
//import java.util.HashMap;
//import java.util.Map;
//public class BufferSerializableUtil {
//    private static Map<Class, Parser<MessageLite>> map = new HashMap<>();
//
//    private static <M extends MessageLite> com.google.protobuf.Parser<M> doParser(Class<M> type) {
//        if (map.containsKey(type)) {
//            return (AbstractParser)map.get(type);
//        } else {
//            AbstractParser<M> abstractParser = new AbstractParser<M>()
//            {
//                public M parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) {
//                    try {
//                        Constructor c = type.getDeclaredConstructor(CodedInputStream.class, ExtensionRegistryLite.class);
//                        c.setAccessible(true);
//                        M obj = (MessageLite)c.newInstance(input, extensionRegistry);
//                        return obj;
//                    } catch (Exception var5) {
//                        var5.printStackTrace();
//                        return null;
//                    }
//                }
//            };
//            map.put(type, abstractParser);
//            return abstractParser;
//        }
//    }
//    public static <M extends MessageLite> M parser(Class<M> type,byte[] buff) throws InvalidProtocolBufferException {
//        com.google.protobuf.Parser<M> mParser = doParser(type);
//        return mParser.parseFrom(buff);
//    }
//    public static byte[] toByteArray(com.google.protobuf.Message.Builder builder){
//        return builder.build().toByteArray();
//    }
//
//    public static void main(String[] args) throws InvalidProtocolBufferException {
//        Primitive.PbInteger.Builder builder = Primitive.PbInteger.newBuilder();
//        builder.setValue(100);
//        byte[] bytes = toByteArray(builder);
//        Primitive.PbInteger parser = parser(Primitive.PbInteger.class, bytes);
//        System.out.println(parser.getValue());
//    }
//}
