package com.gabriele.actor.internals;

import android.content.Context;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractActor implements ActorInterface {

    private ActorContext context;
    private final ConcurrentLinkedQueue<ActorMessage> mailbox = new ConcurrentLinkedQueue<>();

    public void preStart() {

    }

    public void afterStop() {

    }

    public ConcurrentLinkedQueue<ActorMessage> getMailbox() {
        return mailbox;
    }

    public ActorContext getActorContext() {
        return context;
    }

    public void setActorContext(ActorContext context) {
        this.context = context;
    }

    public ActorRef getSender() {
        return getActorContext().getSender();
    }

    public ActorRef getSelf() {
        return getActorContext().getSelf();
    }

    public ActorSystem getSystem() {
        return getActorContext().getSystem();
    }

    public void stash() {
        getActorContext().stash();
    }

    public void unstashAll() {
        getActorContext().unstashAll();
    }

    public void become(OnReceiveFunction fun) {
        getActorContext().become(fun);
    }

    public void unbecome() {
        getActorContext().unbecome();
    }

    public Context getContext() {
        return getActorContext().getContext();
    }
}

