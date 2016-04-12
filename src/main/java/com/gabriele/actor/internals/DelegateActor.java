package com.gabriele.actor.internals;

import java.lang.ref.WeakReference;

public class DelegateActor extends AbstractActor {

    protected WeakReference<ActorInterface> delegate;

    public DelegateActor(WeakReference<ActorInterface> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onReceive(Object message) {
        ActorInterface actor = delegate.get();
        if (actor != null) {
            actor.onReceive(message);
        }
    }
}
