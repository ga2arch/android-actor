package com.gabriele.actor.android;

import android.os.Bundle;

import com.gabriele.actor.dispatchers.MainThreadDispatcher;
import com.gabriele.actor.interfaces.OnReceiveFunction;
import com.gabriele.actor.internals.AbstractActor;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FragmentActor extends AbstractActor {

    public static final String EXTRA_PATH = "PATH";

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private WeakReference<FragmentAsActor> delegate;
    private ScheduledFuture destroyFuture;

    public FragmentActor(FragmentAsActor delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    @Override
    public void onCreate() {
        getActorContext().setDispatcher(MainThreadDispatcher.getInstance());
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PATH, getSelf().getPath());
        delegate.get().setArguments(bundle);
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof FragmentCreatedMessage) {
            delegate = ((FragmentCreatedMessage) o).getRef();
            if (destroyFuture != null) {
                destroyFuture.cancel(true);
            }
            become(ready);
            unstashAll();

        } else {
            stash();
        }
    }

    OnReceiveFunction ready = new OnReceiveFunction() {
        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof FragmentPausedMessage) {
                become(paused);

            } else if (!(o instanceof Internal)) {
                delegate.get().onReceive(o);
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
                if (destroyFuture != null) destroyFuture.cancel(true);
                destroyFuture = service.schedule(new Runnable() {
                    @Override
                    public void run() {
                        stopSelf();
                    }
                }, 1, TimeUnit.SECONDS);

            } else if (o instanceof FragmentCreatedMessage) {
                unbecome();
                unbecome();
                unstashAll();
                getSelf().tell(o, getSender());

            } else {
                stash();
            }
        }
    };

    public static class FragmentCreatedMessage implements Internal {
        private WeakReference<FragmentAsActor> ref;

        public FragmentCreatedMessage(FragmentAsActor fragment) {
            this.ref = new WeakReference<>(fragment);
        }

        public WeakReference<FragmentAsActor> getRef() {
            return ref;
        }
    }
    public static class FragmentResumedMessage implements Internal {}
    public static class FragmentPausedMessage implements Internal {}
    public static class FragmentDetachedMessage implements Internal {}

    private interface Internal {}

}
