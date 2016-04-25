package com.gabriele.actor.android;

import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.gabriele.actor.dispatchers.MainThreadDispatcher;
import com.gabriele.actor.eventbus.EventBus;
import com.gabriele.actor.interfaces.WithReceive;
import com.gabriele.actor.internals.AbstractActor;
import com.gabriele.actor.internals.ActorContext;
import com.gabriele.actor.internals.ActorRef;
import com.gabriele.actor.internals.ActorSystem;
import com.gabriele.actor.internals.Props;

public abstract class FragmentAsActor extends Fragment implements WithReceive {

    private ActorSystem system;
    private ActorRef ref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        system = ((ActorApplication) getActivity().getApplicationContext()).getSystem();
        String path = getArguments().getString(FragmentActor.EXTRA_PATH);
        if (path != null) {
            ref = system.actorSelection(path);

        } else {
            ref = system.actorOf(Props.create(this).withDispatcher(MainThreadDispatcher.getInstance()));
        }

        if (ref != null) {
            getSelf().tell(new FragmentActor.FragmentCreatedMessage(), getSelf());

        } else {
            throw new RuntimeException("Actor not created!!");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getSelf().tell(new FragmentActor.FragmentStartedMessage(), getSelf());
    }

    @Override
    public void onResume() {
        super.onResume();
        getSelf().tell(new FragmentActor.FragmentResumedMessage(), getSelf());
    }


    @Override
    public void onPause() {
        super.onPause();
        getSelf().tell(new FragmentActor.FragmentPausedMessage(), getSelf());
    }


    @Override
    public void onDetach() {
        super.onDetach();
        getSelf().tell(new FragmentActor.FragmentDetachedMessage(), getSelf());
    }

    protected ActorRef getSelf() {
        return ref;
    }

    protected ActorContext getActorContext() {
        AbstractActor actor = system.getActor(getSelf());
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
