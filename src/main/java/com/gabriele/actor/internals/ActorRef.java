package com.gabriele.actor.internals;

import java.lang.ref.WeakReference;

public class ActorRef extends WeakReference<Actor> {

    private ActorSystem system;

    public ActorRef(ActorSystem system, Actor actor) {
        super(actor);
        this.system = system;
    }

    public void tell(final Object message, final ActorRef sender) {
        system.getDispatcher().publish(this, message, sender);
    }

    @Override
    public Actor get() {
        if (super.get() == null)
            throw new RuntimeException();

        return super.get();
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }
}
