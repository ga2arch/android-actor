package com.gabriele.actor.internals;

import com.gabriele.actor.exceptions.ActorIsTerminatedException;

public class ActorRef {

    private ActorSystem system;
    private final String path;
    private final String name;
    private boolean isTerminated;

    public ActorRef(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public void setSystem(ActorSystem system) {
        this.system = system;
    }

    public void tell(final Object message, final ActorRef sender) {
        if (isTerminated()) throw new ActorIsTerminatedException();

        system.publish(this, message, sender);
    }

    public static ActorRef noSender() {
        return null;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setTerminated() {
        isTerminated = true;
    }

    public boolean isTerminated() {
        return isTerminated;
    }
}
