package com.gabriele.actor.internals;

import java.lang.ref.WeakReference;

public abstract class AbstractDelegateActor extends AbstractActor {

    protected WeakReference<ActorInterface> delegate;

    @Override
    public void onReceive(Object message) {
        ActorInterface actor = delegate.get();
        if (actor != null) {
            actor.onReceive(message);
        }
    }
}
