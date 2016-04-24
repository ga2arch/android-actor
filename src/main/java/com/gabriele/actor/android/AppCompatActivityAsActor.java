package com.gabriele.actor.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.gabriele.actor.dispatchers.MainThreadDispatcher;
import com.gabriele.actor.eventbus.EventBus;
import com.gabriele.actor.interfaces.WithReceive;
import com.gabriele.actor.internals.AbstractActor;
import com.gabriele.actor.internals.ActorContext;
import com.gabriele.actor.internals.ActorRef;
import com.gabriele.actor.internals.ActorSystem;
import com.gabriele.actor.internals.Props;

public abstract class AppCompatActivityAsActor extends AppCompatActivity implements WithReceive {

    private ActorSystem system;
    private ActorRef ref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        system = ((ActorApplication) getApplicationContext()).getSystem();
        String path = getIntent().getStringExtra(ActivityActor.EXTRA_PATH);
        if (path != null) {
            ref = system.actorSelection(path);
        } else {
            ref = system.actorOf(Props.create(this).withDispatcher(MainThreadDispatcher.getInstance()));
        }

        if (ref != null) {
            getSelf().tell(
                    new ActivityActor.ActivityCreatedMessage(this), getSelf());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSelf().tell(new ActivityActor.ActivityResumedMessage(), getSelf());
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSelf().tell(new ActivityActor.ActivityPausedMessage(), getSelf());
        getEventBus().unsubscribe(ref);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        system.terminateActor(getSelf().get());
    }

    protected ActorRef getSelf() {
        return ref;
    }

    protected ActorContext getActorContext() {
        AbstractActor actor = ref.get();
        if (actor != null)
            return actor.getActorContext();
        else
            return null;
    }

    protected ActorRef getSender() {
        return getActorContext().getSender();
    }

    protected EventBus getEventBus() {
        return system.getEventBus();
    }
}