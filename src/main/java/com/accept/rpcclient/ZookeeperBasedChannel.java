package com.accept.rpcclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-16 下午5:04
 */
public class ZookeeperBasedChannel implements Client {
    private class ZookeeperWatcher implements Watcher {

        @Override
        public void  process(WatchedEvent event) {
            String path = event.getPath();
            if (event.getType() == Event.EventType.None) {

            } else if (event.getType() == Event.EventType.NodeCreated) {

            } else if (event.getType() == Event.EventType.NodeDataChanged) {

            } else if (event.getType() == Event.EventType.NodeDeleted) {

            } else if (event.getType() == Event.EventType.NodeChildrenChanged) {

            }
        }
    }

    @Override
    public boolean isHealthy() {
        return false;
    }

    @Override
    public Object getProxy() {
        return null;
    }
}
