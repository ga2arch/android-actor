package com.gabriele.actor.internals;

import java.lang.ref.WeakReference;

public class ActorRef extends WeakReference<AbstractActor> {

    private int hashCode;

    public ActorRef(AbstractActor actor) {
        super(actor);
        hashCode = actor.hashCode();
    }

    public void tell(final Object message, final ActorRef sender) {
        get().getSystem().publish(this, message, sender);
    }

    @Override
    public AbstractActor get() {
        if (super.get() == null)
            throw new ActorIsTerminatedException();

        return super.get();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
