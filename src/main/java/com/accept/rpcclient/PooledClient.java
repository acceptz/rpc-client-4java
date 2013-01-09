package com.accept.rpcclient;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 * Author: Hong Zhu(hong.zhu@renren-inc.com)
 * Date: 13-1-9 下午4:10
 */
public abstract class PooledClient implements Client {
    protected abstract Connection createConnection();

    private static final int DEFAULT_MAX_IDLE = 1000;
    private static final int DEFAULT_INIT_IDLE = 50;

    private static final int MAX_ALLOWABLE_FAILURES = 5;
    private static final long RETRY_INTERVAL = 10000L;

    private StackObjectPool<Connection> pool = new StackObjectPool<Connection>(new ConnectionFactory(), DEFAULT_MAX_IDLE, DEFAULT_INIT_IDLE);

    @Override
    public boolean isHealthy() {
        return true;
    }

    private void markHealthy() {

    }

    private void markUnHealthy() {

    }

    private void didFail() {

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
