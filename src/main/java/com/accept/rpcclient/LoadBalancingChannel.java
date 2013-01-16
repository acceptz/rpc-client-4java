package com.accept.rpcclient;

import com.accept.rpcclient.exceptions.ClientUnavailableException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A channel multiplexes requests over several servers.
 * Author: Hong Zhu(acceptzh@gmail.com)
 * Date: 13-1-16 下午4:11
 */
public class LoadBalancingChannel implements Client {
    private List<Client> underlying;
    private Random rnd = new Random();

    public LoadBalancingChannel(List<Client> underlying) {
        this.underlying = new ArrayList<Client>();

        for (Client cl : underlying) {
            this.underlying.add(cl);
        }
    }

    @Override
    public boolean isHealthy() {
        for (Client c : underlying) {
            if (c.isHealthy()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getProxy() {
        List<Client> underlyingH = healthyFilter(underlying);
        int size = underlyingH.size();

        if (size <= 0) {
            throw new ClientUnavailableException("No pooled client available");
        }

        Client cl = underlyingH.get(rnd.nextInt(size));
        return cl.getProxy();
    }

    private List<Client> healthyFilter(List<Client> clients) {
        List<Client> result = new ArrayList<Client>();
        for (Client c : clients) {
            if (c.isHealthy()) {
                result.add(c);
            }
        }
        return result;
    }
}
