package com.gabriele.actor.logging;

import android.util.Log;

import com.gabriele.actor.internals.ActorRef;

public class Logger {

    private final ActorRef actorRef;

    public static Logger create(ActorRef ref) {
        return new Logger(ref);
    }

    private Logger(ActorRef actorRef) {
        this.actorRef = actorRef;
    }

    public void info(String str) {
        Log.i(actorRef.getName(), String.format("%s: %s", actorRef.getPath(), str));
    }

    public void debug(String str) {
        Log.d(actorRef.getName(), String.format("%s: %s", actorRef.getPath(), str));
    }

}
