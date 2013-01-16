package com.accept.rpcclient.events;

import com.accept.rpcclient.PooledClient;
import com.accept.rpcclient.PooledClient.EventType;

/**
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-15 下午5:46
 */
public class TimedOutEvent implements ClientEvent {
    //Time this event was generated.
    private long time;
    private EventType type;

    public TimedOutEvent(long now) {
        this.time = now;
        this.type = EventType.TIMEDOUT_EVENT;
    }

    public long getTime() {
        return time;
    }

    public EventType getType() {
        return type;
    }
}
