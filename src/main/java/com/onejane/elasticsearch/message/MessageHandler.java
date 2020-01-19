package com.onejane.elasticsearch.message;


public interface MessageHandler<T> {

    void handler(T t);

}
