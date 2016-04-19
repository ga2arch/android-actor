package com.gabriele.actor.internals;

import com.gabriele.actor.exceptions.ActorIsTerminatedException;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class ActorRef extends WeakReference<AbstractActor> {

    private int hashCode;
    private ActorSystem system;

    public ActorRef(AbstractActor actor) {
        super(actor);
        hashCode = actor.hashCode();
    }

    public void setSystem(ActorSystem system) {
        this.system = system;
    }

    public void tell(final Object message, final ActorRef sender) {
        system.publish(this, message, sender);
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

    @Override
    public boolean equals(Object other) {
        if (other instanceof AbstractActor) {
            return get() == other;
        }
        if (other instanceof Reference) {
            return get() == ((Reference) other).get();
        }
        return get() == other;
    }
}
