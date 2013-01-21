package com.accept.rpcclient;

import com.accept.rpcclient.events.ClientEvent;
import com.accept.rpcclient.events.HealthyEvent;
import com.accept.rpcclient.events.TimedOutEvent;
import com.accept.rpcclient.events.UnhealthyEvent;
import com.accept.rpcclient.exceptions.ClientUnavailableException;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulate communication to a single RPC endpoint.
 * The "client" is the abstract endpoint, a "connection" is a concrete connection to said client.
 *
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-9 下午4:10
 */
public abstract class PooledClient implements Client {
    private static final Logger logger = LoggerFactory.getLogger(PooledClient.class.getName());

    private static final int DEFAULT_MAX_IDLE = 1000;
    private static final int DEFAULT_INIT_IDLE = 50;

    private static final int MAX_ALLOWABLE_FAILURES = 5;
    private static final long RETRY_INTERVAL = 10000L;

    private int maxAllowableFailures = MAX_ALLOWABLE_FAILURES;
    private long retryInterval = RETRY_INTERVAL;

    private AtomicInteger numUnhealthy = new AtomicInteger(0);
    private AtomicInteger numFailures = new AtomicInteger(0);
    private volatile long unhealthyAt = 0L;

    private StackObjectPool<Connection> pool = new StackObjectPool<Connection>(
            new ConnectionFactory(), DEFAULT_MAX_IDLE, DEFAULT_INIT_IDLE);

    public static enum EventType {
        TIMEDOUT_EVENT, HEALTHY_EVENT, UNHEALTHY_EVEN
    }

    protected abstract Connection createConnection();

    protected abstract void handleEvent(ClientEvent e);

    public abstract Class getTargetInterface();

    @Override
    public Object getProxy() {
        ClientProxy clientProxy = new ClientProxy(getClass().getClassLoader());
        return clientProxy.getProxy();
    }

    @Override
    public boolean isHealthy() {
        if (unhealthyAt == 0L) {
            return true;
        }

        long interval = System.currentTimeMillis() - unhealthyAt;
        if (interval >= retryInterval) {
            markHealthy();
            return true;
        }
        return false;
    }

    private void markHealthy() {
//        logger.info(getPooledClientName() + " went healthy at " + DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(System.currentTimeMillis()));
        long now = System.currentTimeMillis();
        logEvent(new HealthyEvent(now, now-unhealthyAt));

        unhealthyAt = 0L;
        numFailures.set(0);
    }

    private void markUnHealthy() {
        unhealthyAt = System.currentTimeMillis();
        numUnhealthy.incrementAndGet();

        logEvent(new UnhealthyEvent(System.currentTimeMillis()));

//        logger.error(getPooledClientName() + " went unhealthy at " +
//                DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(this.unhealthyAt) +
//                ". Total unhealthy times is " + numUnhealthy);
    }

    private void didSucceed() {
        markHealthy();
    }

    private void didFail() {
        if (numFailures.incrementAndGet() > maxAllowableFailures) {
            markUnHealthy();
        }
    }

    private Connection get() {
        Connection conn = null;
        if (isHealthy()) {
            try {
                conn = pool.borrowObject();
            } catch (Exception e) {
                didFail();
                logger.error("Get connection from pool failed.", e);
            }
        }
        return conn;
    }

    private void put(Connection conn) {
        if (conn.isHealthy()) {
            try {
                pool.returnObject(conn);
                didSucceed();
            } catch (Exception e) {
                didFail();
                logger.error("Return connection to pool failed.", e);
            }
        } else {
            //Relinquish this connection from the pool.
            didFail();
        }
    }

    private void logEvent(ClientEvent e) {
        handleEvent(e);
    }

    public class ClientProxy implements InvocationHandler {
        private ClassLoader classLoader;

        public ClientProxy(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public Object getProxy() {
            return Proxy.newProxyInstance(classLoader, getTargetInterface().getInterfaces(), this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //Since rpc doesn't support overloading,so just use method.getName() for tags.
            StopWatch stopWatch = new Slf4JStopWatch();

            Connection conn = get();
            if (conn == null) {
                stopWatch.stop(method.getName() + ".client_unavailable");
                throw new ClientUnavailableException("No connection is available.");
            }

            Object result = null;
            try {
                Object client = conn.getClient();
                result = method.invoke(client, args);

                stopWatch.stop(method.getName() + ".success");
            } catch (Exception e) {
                ErrorType errorType = conn.unwrapException(e);

                switch (errorType) {
                    case TIMEOUT_ERROR:
                        stopWatch.stop(method.getName() + ".timeout");
                        logEvent(new TimedOutEvent(System.currentTimeMillis()));
                        conn.markFailed();
                        break;
                    case UNKNOWN_ERROR:
                        stopWatch.stop(method.getName() + ".unknown_error");
                        conn.markFailed();
                        break;
                    case IGNORE_ERROR:
                        stopWatch.stop(method.getName() + ".ignore_error");
                        //Do nothing here
                        break;
                    default:
                        stopWatch.stop(method.getName() + ".critical_error");
                        //Can't be here, log it.
                }
            } finally {
                put(conn);
            }
            return result;
        }
    }

    private class ConnectionFactory implements PoolableObjectFactory<Connection> {
        @Override
        public Connection makeObject() throws Exception {
            Connection conn = createConnection();
            conn.ensureOpen();
            return conn;
        }

        @Override
        public void destroyObject(Connection obj) throws Exception {
            obj.tearDown();
        }

        @Override
        public boolean validateObject(Connection obj) {
            return obj.isHealthy();
        }

        @Override
        public void activateObject(Connection obj) throws Exception {
            obj.ensureOpen();
        }

        @Override
        public void passivateObject(Connection obj) throws Exception {
            obj.flush();
        }
    }
}
