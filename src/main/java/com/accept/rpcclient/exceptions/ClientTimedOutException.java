package com.accept.rpcclient.exceptions;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-15 上午11:49
 */
public class ClientTimedOutException extends RuntimeException {
    public ClientTimedOutException() {
        super();
    }

    public ClientTimedOutException(String message) {
        super(message);
    }

    public ClientTimedOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientTimedOutException(Throwable cause) {
        super(cause);
    }
}
