package com.accept.rpcclient;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-16 下午5:04
 */
public class ZookeeperBasedChannel implements Client {
    private final static Logger logger = LoggerFactory.getLogger(ZookeeperBasedChannel.class);

    private String zkRootPath;
    private String serviceName;
    private String serviceNodesFolder;

    private String zookeeperConnectString;
    private int sessionTimeout = 300000;
    private ZookeeperWatcher zkWatcher;
    private ZooKeeper zk;

    private CountDownLatch latch;

    private final Pattern ipPattern = Pattern.compile("[\\d]{1,3}(\\.[\\d]{1,3}){1,3}:[\\d]{1,5}");

    public ZookeeperBasedChannel(String zkRootPath, String serviceName, String serviceNodesFolder) {
        this.zkRootPath = zkRootPath;
        this.serviceName = serviceName;
        this.serviceNodesFolder = serviceNodesFolder;

        zkWatcher = new ZookeeperWatcher();
    }

    private ZooKeeper getZookeeper() {
        if (zk == null) {
            synchronized (ZookeeperBasedChannel.class) {
                if (zk == null) {
                    long startTime = System.currentTimeMillis();

                    try {
                        latch = new CountDownLatch(1);
                        zk = makeClient();

                        latch.await(30, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        zk = null;
                        logger.error("Interrupted when invoking getZookeeper()", e);
                    } finally {
                        latch = null;
                        if (zk != null) {
                            logger.info("Init zookeeper SUCCEED after " + (System.currentTimeMillis() - startTime) + "ms.");
                        } else {
                            logger.info("Init zookeeper FAILED after " + (System.currentTimeMillis() - startTime) + "ms.");
                        }
                    }
                }
            }
        }
        return zk;
    }

    private ZooKeeper makeClient() {
        try {
            return new ZooKeeper(zookeeperConnectString, sessionTimeout, zkWatcher);
        } catch (IOException e) {
            throw new RuntimeException("Can't connect to zookeeper server[" + zookeeperConnectString +
                    "][" + sessionTimeout + "]", e);
        }
    }

    private class ZookeeperWatcher implements Watcher {

        @Override
        public void  process(WatchedEvent event) {
            String path = event.getPath();
            if (event.getType() == Event.EventType.None) {
                switch (event.getState()) {
                    case SyncConnected:
                        //Connected to the zookeeper server.
                        //Init/Refresh local service info.
                        if (latch != null) {
                            latch.countDown();
                        }

                        break;
                    case Expired:
                        break;
                }
            } else if (event.getType() == Event.EventType.NodeCreated) {

            } else if (event.getType() == Event.EventType.NodeDataChanged) {

            } else if (event.getType() == Event.EventType.NodeDeleted) {

            } else if (event.getType() == Event.EventType.NodeChildrenChanged) {

            }
        }
    }

    private void refreshServiceInfo() {
        List<String> services = getServiceInfo();
        if (services == null || services.size() == 0) {
            logger.error("We get null services when refreshing service info");
            return;
        }


    }

    private List<String> getServiceInfo() {
        List<String> result = null;
        String zkServicePath = zkRootPath + serviceName + serviceNodesFolder;
        try {
            Stat stat = zk.exists(zkServicePath, false);
            if (stat == null) {
                return null;
            }

            List<String> serviceList = zk.getChildren(zkServicePath, false);
            //Element in serviceList must match the format of ip:port.
            //We need to check it.
            result = new LinkedList<String>();

            for (String child : serviceList) {
                if (validateIp(child)) {
                    result.add(child);
                }
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean validateIp(String ip) {
        return ipPattern.matcher(ip).matches();
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
