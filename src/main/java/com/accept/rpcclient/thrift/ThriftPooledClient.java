package com.accept.rpcclient.thrift;

import com.accept.rpcclient.Connection;
import com.accept.rpcclient.PooledClient;
import com.accept.rpcclient.events.ClientEvent;
import com.accept.rpcclient.events.HealthyEvent;
import com.accept.rpcclient.events.UnhealthyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-16 上午11:26
 */
public class ThriftPooledClient extends PooledClient {
    private static final Logger logger = LoggerFactory.getLogger(ThriftPooledClient.class);

    private String host;
    private int port;
    private int connTimeout;
    private int soTimeout;
    private boolean framed;
    private Class targetInterface;
    private Class clientClass;

    public ThriftPooledClient(String host, int port, int connTimeout, int soTimeout, boolean framed, Class targetInterface, Class clientClass) {
        this.host = host;
        this.port = port;
        this.connTimeout = connTimeout;
        this.soTimeout = soTimeout;
        this.framed = framed;

        this.targetInterface = targetInterface;
        this.clientClass = clientClass;
    }

    @Override
    protected Connection createConnection() {
        return new ThriftConnection(host, port, connTimeout, soTimeout, framed, clientClass);
    }

    @Override
    protected void handleEvent(ClientEvent e) {
        switch (e.getType()) {
            case HEALTHY_EVENT:
                logger.info(String.format("Pooled client %s became healthy after being unhealthy for %d ms", host+":"+port, ((HealthyEvent)e).getUnhealthyTime()));
                break;
            case UNHEALTHY_EVEN:
                logger.error(String.format("Pooled client %s became unhealthy at %d", host+":"+port, ((UnhealthyEvent)e).getTime()));
                break;
            case TIMEDOUT_EVENT:
                logger.error(String.format("Timeout for pooled client %s", host+":"+port));
                break;
            default:
                logger.error(String.format("Unknown error for pooled client %s", host+":"+port));
        }
    }

    @Override
    public Class getTargetInterface() {
        if (targetInterface == null) {
            throw new RuntimeException("targetInterface can't be null!");
        }

        return targetInterface;
    }

    public void setTargetInterface(Class targetInterface) {
        this.targetInterface = targetInterface;
    }
}
