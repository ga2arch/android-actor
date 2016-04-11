package com.gabriele.actor.internals;

public class Message {
    Object object;
    ActorRef sender;

    public Message(Object object, ActorRef sender) {
        this.object = object;
        this.sender = sender;
    }

    public Object getObject() {
        return object;
    }

    public ActorRef getSender() {
        return sender;
    }
}
