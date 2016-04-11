package com.gabriele.actor.internals;

import android.app.Activity;

import java.lang.ref.WeakReference;

public class ActivityActor extends AbstractDelegateActor {

    public ActivityActor(Activity delegate) {
        if (delegate instanceof ActorInterface) {
            this.delegate = new WeakReference<>((ActorInterface) delegate);
        }
    }

    @Override
    public void onReceive(final Object message) {
        delegate.get().onReceive(message);
    }
}
