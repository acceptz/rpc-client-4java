package com.accept.rpcclient.events;


import com.accept.rpcclient.PooledClient;
import com.accept.rpcclient.PooledClient.EventType;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-15 下午6:57
 */
public class UnhealthyEvent implements ClientEvent {
    private long time;
    private EventType type;

    public UnhealthyEvent(long now) {
        this.time = now;
        this.type = EventType.UNHEALTHY_EVEN;
    }

    public long getTime() {
        return time;
    }

    public EventType getType() {
        return type;
    }
}
