package com.gabriele.actor.internals;

import com.gabriele.actor.exceptions.ActorIsTerminatedException;

import java.lang.ref.WeakReference;

public class ActorRef extends WeakReference<AbstractActor> {

    private int hashCode;

    public ActorRef(AbstractActor actor) {
        super(actor);
        hashCode = actor.hashCode();
    }

    public void tell(final Object message, final ActorRef sender) {
        get().getActorContext().getSystem().publish(this, message, sender);
    }

    public static ActorRef noSender() {
        return null;
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
