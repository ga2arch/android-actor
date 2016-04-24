package com.gabriele.actor.logging;

import com.gabriele.actor.internals.AbstractActor;
import com.gabriele.actor.internals.ActorRef;

public class Logger {

    private String path;

    public static Logger create(ActorRef ref) {
        return new Logger(ref.getPath());
    }

    public static Logger create(AbstractActor ref) {
        return new Logger(ref.getSelf().getPath());
    }

    private Logger(String path) {
        this.path = path;
    }

    public void info(String str) {
        System.out.println(String.format("%s: %s", path, str));
    }

}
