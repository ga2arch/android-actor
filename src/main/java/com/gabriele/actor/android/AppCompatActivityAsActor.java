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

        } else if (savedInstanceState != null) {
            ref = system.actorSelection("//" + getClass().getSimpleName());

        }
        //FIXME recreate actor with the same path

        if (ref == null) {
            ref = system.actorOf(Props.create(this)
                    .withDispatcher(MainThreadDispatcher.getInstance()),
                    getClass().getSimpleName());
        }
    }

    @Override
    protected void onStart() {
        getSelf().tell(new ActivityActor.ActivityCreatedMessage(this), getSelf());
        super.onStart();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        getSelf().tell(new ActivityActor.ActivityConfigChangedMessage(), getSelf());
        return super.onRetainCustomNonConfigurationInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSelf().tell(new ActivityActor.ActivityResumedMessage(), getSelf());
    }

    @Override
    protected void onPause() {
        super.onPause();
        getEventBus().unsubscribe(getSelf());
        getSelf().tell(new ActivityActor.ActivityPausedMessage(), getSelf());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSelf().tell(new ActivityActor.ActivityDestroyedMessage(), getSelf());
    }

    public ActorRef getSelf() {
        return ref;
    }

    public ActorContext getActorContext() {
        AbstractActor actor = system.getActor(getSelf());
        if (actor != null)
            return actor.getActorContext();
        else
            return null;
    }

    protected ActorRef getSender() {
        return getActorContext().getSender();
    }

    public EventBus getEventBus() {
        return system.getEventBus();
    }
}
