package com.gabriele.actor.android;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import com.gabriele.actor.interfaces.WithReceive;
import com.gabriele.actor.internals.DelegateActor;

import java.lang.ref.WeakReference;

public class ActivityActor extends DelegateActor {

    public ActivityActor(Activity delegate) {
        super(new WeakReference<>((WithReceive) delegate));
    }

    public ActivityActor(AppCompatActivity delegate) {
        super(new WeakReference<>((WithReceive) delegate));
    }

    @Override
    public void preStart() {
        super.preStart();
    }

    @Override
    public void onReceive(final Object o) throws Exception {
        delegate.get().onReceive(o);
    }
}
