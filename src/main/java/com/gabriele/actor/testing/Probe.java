package com.gabriele.actor.testing;

import com.gabriele.actor.internals.ActorRef;

public class Probe {

    private ActorRef sender;
    private ActorRef receiver;
    private Object   message;

    public ActorRef getSender() {
        return sender;
    }

    public void setSender(ActorRef sender) {
        this.sender = sender;
    }

    public ActorRef getReceiver() {
        return receiver;
    }

    public void setReceiver(ActorRef receiver) {
        this.receiver = receiver;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
        synchronized (Probe.class) {
            Probe.class.notify();
        }
    }

    public void expectMessage(Object msg, long wait) {
        synchronized (Probe.class) {
            try {
                Probe.class.wait(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (message == null) {
            throw new RuntimeException("message didn't arrive");
        }
        else if (message != msg) {
            throw new RuntimeException("received wrong message: " + message);
        }
    }
}
