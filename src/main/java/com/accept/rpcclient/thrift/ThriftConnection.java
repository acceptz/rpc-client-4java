package com.accept.rpcclient.thrift;

import com.accept.rpcclient.Client;
import com.accept.rpcclient.Connection;
import com.accept.rpcclient.Client.ErrorType;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-10 下午5:52
 */
public class ThriftConnection implements Connection {
    private static final Logger logger = LoggerFactory.getLogger(ThriftConnection.class.getName());
    private String host;
    private int port;
    private int soTimeout = 1000;
    private int conTimeout = 5000;
    private boolean framed;

    private TSocket socket;
    private TTransport transport;
    private TProtocol protocol;

    private boolean didFail = false;
    private boolean didFailConnect = false;

    private Class clientClass;
    private Object client;

    public ThriftConnection(String host, int port, int conTimeOut, int soTimeout, boolean framed, Class clientClass) {
        this.host = host;
        this.port = port;
        this.conTimeout = conTimeOut;
        this.soTimeout = soTimeout;
        this.framed = framed;

        this.socket = new TSocket(host, port, this.conTimeout);
        this.transport = (framed)?new TFramedTransport(socket):socket;
        this.protocol = new TBinaryProtocol(transport);

        if (clientClass == null) {
            throw new RuntimeException("constructor can not be null!");
        }

        this.clientClass = clientClass;

        try {
            client = clientClass.getConstructor(TProtocol.class).newInstance(protocol);
        } catch (NoSuchMethodException e) {
            logger.error("Error occurred when getting client constructor!", e);
            throw new RuntimeException("Error occurred when getting client constructor!", e);
        } catch (InvocationTargetException e) {
            logger.error("The underlying constructor error when getting client Object!", e);
            throw new RuntimeException("The underlying constructor error when getting client Object!", e);
        } catch (InstantiationException e) {
            logger.error("The client class can not be abstract!", e);
            throw new RuntimeException("The client class can not be abstract!", e);
        } catch (IllegalAccessException e) {
            logger.error("The constructor is inaccessible when getting client Object!", e);
            throw new RuntimeException("The constructor is inaccessible when getting client Object!", e);
        }
    }



    @Override
    public Object getClient() {
        return client;
    }

    @Override
    public void ensureOpen() {
        if (transport == null) {
            didFailConnect =true;
        }

        if (transport.isOpen()) {
            return;
        }

        try {
            transport.open();
            socket.setTimeout(this.soTimeout);
        } catch (TTransportException e) {
            logger.error("Transport open error![" + host + "][" + port + "]", e);
            didFailConnect = true;
        }

    }

    @Override
    public void tearDown() {
        if (transport == null) {
            return;
        }
        transport.close();
    }

    // Flushing framed transports leaves it in some weird state.
    @Override
    public void flush() {
    }

    @Override
    public ErrorType unwrapException(Exception e) {
        if (e == null) {
            return ErrorType.IGNORE_ERROR;
        }
        if (e instanceof TTransportException) {
            if (((TTransportException) e).getType() == TTransportException.TIMED_OUT)
                return ErrorType.TIMEOUT_ERROR;
            if (e.getCause().getClass() == SocketTimeoutException.class) {
                return ErrorType.TIMEOUT_ERROR;
            }
        }
        return ErrorType.UNKNOWN_ERROR;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public int getConTimeout() {
        return this.conTimeout;
    }

    @Override
    public int getTimeout() {
        return this.soTimeout;
    }

    @Override
    public boolean isHealthy() {
        return !(didFail || didFailConnect);
    }

    @Override
    public void markFailed() {
        this.didFail = true;
    }
}
