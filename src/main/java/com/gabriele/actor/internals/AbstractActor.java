package com.gabriele.actor.internals;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractActor implements ActorInterface {

    private ActorSystem system;
    private ActorRef sender;
    private ActorRef self;
    private AbstractDispatcher dispatcher;
    private ConcurrentLinkedQueue<Message> mailbox = new ConcurrentLinkedQueue<>();

    public void preStart() {

    }

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

    public AbstractDispatcher getDispatcher() {
        return dispatcher;
    }

    public ConcurrentLinkedQueue<Message> getMailbox() {
        return mailbox;
    }

    public ActorSystem getSystem() {
        return system;
    }

    public void setSystem(ActorSystem system) {
        this.system = system;
    }

    public void setDispatcher(AbstractDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
}
