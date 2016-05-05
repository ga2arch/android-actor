package com.gabriele.actor.internals;

public class ActorMessage {
    Object object;
    ActorRef sender;

    public ActorMessage(Object object, ActorRef sender) {
        this.object = object;
        this.sender = sender;
    }

    public Object getObject() {
        return object;
    }

    public ActorRef getSender() {
        return sender;
    }

    public static class PoisonPill implements ControlMessage {}
    public static class Terminated implements ControlMessage {}
    public static class AddChild implements ControlMessage {}

    public interface ControlMessage {}
}
