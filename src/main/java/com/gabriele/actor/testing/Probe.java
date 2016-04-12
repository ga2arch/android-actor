package com.gabriele.actor.testing;

import com.gabriele.actor.internals.ActorRef;

public class Probe {

    private boolean propagate = true;
    private ActorRef sender;
    private ActorRef receiver;
    private Object   message;

    public Probe() {
    }

    public Probe(boolean propagate) {
        this.propagate = propagate;
    }

    public ActorRef getSender() {
        return sender;
    }

    public boolean toPropagate() {
        return propagate;
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
        synchronized (this) {
            notifyAll();
        }
    }

    public void expectMessage(Object msg, long wait) {
        if (message == null)
            synchronized (this) {
                try {
                    wait(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        if (message == null) {
            throw new RuntimeException("message didn't arrive");
        }
        else if (!message.equals(msg)) {
            throw new RuntimeException("received: " + message + " instead of" + msg);
        }
    }
}
