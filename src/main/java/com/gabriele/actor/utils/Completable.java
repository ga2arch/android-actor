package com.gabriele.actor.utils;

import java.util.concurrent.TimeUnit;

public class Completable {
    public Object content;

    public synchronized void put(Object content) {
        this.content = content;
        notify();
    }

    public synchronized Object get(long time, TimeUnit unit) {
        long millis = unit.toMillis(time);
        try {
            wait(millis);
            return content;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

}
