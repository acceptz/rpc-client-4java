package com.accept.rpcclient.events;

import com.accept.rpcclient.PooledClient;
import com.accept.rpcclient.PooledClient.EventType;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-15 下午6:53
 */
public class HealthyEvent implements ClientEvent {
    private long time;
    private long unhealthyTime;
    private EventType type;

    public HealthyEvent(long now, long unhealthyTime) {
        this.time = now;
        this.unhealthyTime = unhealthyTime;
        this.type = EventType.HEALTHY_EVENT;
    }

    public long getTime() {
        return time;
    }

    public long getUnhealthyTime() {
        return unhealthyTime;
    }

    public EventType getType() {
        return type;
    }
}
