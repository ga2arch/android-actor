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
    public static class DeadLetter implements ControlMessage {
        public final Object messsage;
        public final ActorRef recipient;

        public DeadLetter(Object messsage, ActorRef recipient) {
            this.messsage = messsage;
            this.recipient = recipient;
        }
    }
    public interface ControlMessage {}
}
