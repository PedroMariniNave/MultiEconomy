package com.zpedroo.multieconomy.interfaces;

public interface ISerialization<T> {

    String serialize(T object);

    T deserialize(String serialized);
}