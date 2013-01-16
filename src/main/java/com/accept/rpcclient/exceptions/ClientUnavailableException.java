package com.accept.rpcclient.exceptions;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-15 上午11:48
 */
public class ClientUnavailableException extends RuntimeException {
    public ClientUnavailableException() {
        super();
    }

    public ClientUnavailableException(String message) {
        super(message);
    }

    public ClientUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientUnavailableException(Throwable cause) {
        super(cause);
    }
}
