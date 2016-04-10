package com.gabriele.actor.internals;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class Actor {

    private final BlockingQueue<Object> mailbox = new LinkedBlockingDeque<>();
    private ActorRef sender;
    private ActorRef self;

    public void preStart() {

    }

    abstract public void onReceive(Object message);

    void setSender(ActorRef sender) {
        this.sender = sender;
    }

    public ActorRef getSelf() {
        return self;
    }

    public void setSelf(ActorRef self) {
        this.self = self;
    }

    public ActorRef getSender() {
        return sender;
    }
}
