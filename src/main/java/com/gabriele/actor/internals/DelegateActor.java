package com.gabriele.actor.internals;

import com.gabriele.actor.interfaces.WithReceive;

import java.lang.ref.WeakReference;

public class DelegateActor extends AbstractActor {

    protected WeakReference<WithReceive> delegate;

    public DelegateActor(WeakReference<WithReceive> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        WithReceive actor = delegate.get();
        if (actor != null) {
            actor.onReceive(o);
        }
    }
}
