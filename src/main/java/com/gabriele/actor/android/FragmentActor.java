package com.gabriele.actor.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.gabriele.actor.dispatchers.MainThreadDispatcher;
import com.gabriele.actor.interfaces.OnReceiveFunction;
import com.gabriele.actor.internals.AbstractActor;
import com.gabriele.actor.internals.ActorMessage;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;

public class FragmentActor extends AbstractActor {

    public static final String EXTRA_PATH = "PATH";

    private WeakReference<Fragment> currentFragment;
    private WeakReference<FragmentAsActor> delegate;
    private ScheduledFuture destroyFuture;
    private ActorMessage initMessage;

    public FragmentActor(FragmentAsActor delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    @Override
    public void onCreate() {
        getActorContext().setDispatcher(MainThreadDispatcher.getInstance());
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PATH, getSelf().getPath());
        delegate.get().setArguments(bundle);
        become(wait);
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof FragmentPausedMessage) {
            become(paused);

        } else if (!(o instanceof Internal)) {
            delegate.get().onReceive(o);
        }
    }

    OnReceiveFunction wait = new OnReceiveFunction() {
        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof FragmentStartedMessage) {
                unbecome();
                unstashAll();

            } else {
                stash();
            }
        }
    };

    OnReceiveFunction paused = new OnReceiveFunction() {
        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof FragmentResumedMessage) {
                unbecome();
                unstashAll();

            } else if (o instanceof FragmentDetachedMessage) {
                stopSelf();

            } else {
                stash();
            }
        }
    };

    public static class FragmentCreatedMessage implements Internal {}
    public static class FragmentStartedMessage implements Internal {}
    public static class FragmentResumedMessage implements Internal {}
    public static class FragmentPausedMessage implements Internal {}
    public static class FragmentDetachedMessage implements Internal {}

    private interface Internal {}

}
