package com.accept.rpcclient;

/**
 * Author: Hong Zhu(hong.zhu@renren-inc.com)
 * Date: 13-1-9 下午3:53
 */
public interface Connection {
    //ensure the connection is open, called at least prior to use.
    void ensureOpen();

    //Tear down the connection, called before relinquishing this connection from the pool.
    void tearDown();

    //flush is called every time the connection is given back to the pool.
    void flush();

    //Check weather the connection is healthy. This is called before giving connection back to the pool.
    boolean isHealthy();
}
