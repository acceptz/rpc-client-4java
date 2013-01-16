package com.accept.rpcclient;

import com.accept.rpcclient.Client.ErrorType;
/**
 * Connection describe an individual connection to a client.
 * This wraps the RPC client itself, and adorns it with the ability to check connection
 * health, client health and etc.
 *
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-9 下午3:53
 */
public interface Connection {
    //Return the underlying client of type T.
    //The client wraps the rpc client.
    Object getClient();

    String getHost();

    int getPort();

    //Return the timeout when connecting to the server.
    int getConTimeout();

    //Return the timeout when transfer data with server.
    int getTimeout();

    //ensure the connection is open, called at least prior to use.
    void ensureOpen();

    //Tear down the connection, called before relinquishing this connection from the pool.
    void tearDown();

    //flush is called every time the connection is given back to the pool.
    void flush();

    //Check weather the connection is healthy. This is called before giving connection back to the pool.
    boolean isHealthy();

    //Disable this connection.
    //This is called when error occurred during rpc.
    void markFailed();

    //Unwrap exception for upper logic.
    ErrorType unwrapException(Exception e);
}
