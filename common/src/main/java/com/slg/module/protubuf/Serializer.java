package com.slg.module.protubuf;

public interface Serializer {
    <T> Byte[] deserialize(Class<T> tClass ,byte[] bytes);
    <T> Byte[] serialize(T Object);
}
