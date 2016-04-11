package com.gabriele.actor.internals;

import java.lang.ref.WeakReference;

public class ActorRef extends WeakReference<AbstractActor> {

    public ActorRef(AbstractActor actor) {
        super(actor);
    }

    public void tell(final Object message, final ActorRef sender) {
        get().getSystem().publish(this, message, sender);
    }

    @Override
    public AbstractActor get() {
        if (super.get() == null)
            throw new RuntimeException();

        return super.get();
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }
}
