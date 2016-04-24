package com.gabriele.actor.android;

import android.app.Application;

import com.gabriele.actor.internals.ActorSystem;

public class ActorApplication extends Application {

    protected ActorSystem system;

    @Override
    public void onCreate() {
        super.onCreate();
        system = new ActorSystem(getApplicationContext());
    }

    public ActorSystem getSystem() {
        return system;
    }
}
