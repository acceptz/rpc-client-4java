package com.accept.rpcclient;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-9 下午4:08
 */
public interface Client<T> {
    public static enum ErrorType {
        UNKNOWN_ERROR, TIMEOUT_ERROR, IGNORE_ERROR
    }
    boolean isHealthy();

    Object getProxy();
}
