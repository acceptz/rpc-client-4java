package com.accept.rpcclient.events;

import com.accept.rpcclient.PooledClient.EventType;

/**
 * Events that occur in individual clients. They may be observed.
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-15 下午5:41
 */
public interface ClientEvent {
    EventType getType();
}
