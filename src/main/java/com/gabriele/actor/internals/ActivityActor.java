package com.gabriele.actor.internals;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class ActivityActor extends DelegateActor {

    public ActivityActor(Activity delegate) {
        super(new WeakReference<>((ActorInterface) delegate));
    }

    public ActivityActor(AppCompatActivity delegate) {
        super(new WeakReference<>((ActorInterface) delegate));
    }

    @Override
    public void preStart() {
        super.preStart();
    }

    @Override
    public void onReceive(final Object message) {
        delegate.get().onReceive(message);
    }
}
