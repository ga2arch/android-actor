package com.gabriele.actor.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.gabriele.actor.dispatchers.MainThreadDispatcher;
import com.gabriele.actor.interfaces.OnReceiveFunction;
import com.gabriele.actor.interfaces.WithReceive;
import com.gabriele.actor.internals.AbstractActor;

import java.lang.ref.WeakReference;

public class ActivityActor extends AbstractActor {

    public static final String EXTRA_PATH = "PATH";

    private WeakReference<AppCompatActivity> currentActivity;
    private WeakReference<WithReceive> delegate;
    private final Intent intent;

    public ActivityActor(AppCompatActivity currentActivity, Class<?> activityClass) {
        this.intent = new Intent(currentActivity, activityClass);
        this.currentActivity = new WeakReference<>(currentActivity);
    }

    public ActivityActor(AppCompatActivity delegate) {
        this.intent = null;
        this.delegate = new WeakReference<>((WithReceive) delegate);
    }

    @Override
    public void onCreate() {
        getActorContext().setDispatcher(MainThreadDispatcher.getInstance());
        if (intent != null) {
            intent.putExtra(EXTRA_PATH, getSelf().getPath());
            AppCompatActivity activity = currentActivity.get();
            if (activity != null)
                activity.startActivity(intent);
            else
                stopSelf();
        }
    }

    @Override
    public void onReceive(final Object o) throws Exception {
        if (o instanceof ActivityCreatedMessage) {
            delegate = ((ActivityCreatedMessage) o).ref;
            become(ready);
            unstashAll();

        } else {
            stash();
        }
    }

    private OnReceiveFunction ready = new OnReceiveFunction() {
        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof ActivityPausedMessage) {
                become(paused);

            } else if (!(o instanceof Internal)){
                delegate.get().onReceive(o);
            }
        }
    };

    private OnReceiveFunction paused = new OnReceiveFunction() {
        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof ActivityResumedMessage) {
                unbecome();
                unstashAll();

            } else if (o instanceof ActivityConfigChangedMessage) {
                unbecome();
                unbecome();

            } else if (o instanceof ActivityDestroyedMessage) {
                stopSelf();

            } else {
                stash();
            }
        }
    };

    public static class ActivityCreatedMessage implements Internal {
        private WeakReference<WithReceive> ref;

        public ActivityCreatedMessage(WithReceive ref) {
            this.ref = new WeakReference<>(ref);
        }

        public WeakReference<WithReceive> getRef() {
            return ref;
        }
    }

    public static class ActivityConfigChangedMessage implements Internal {}
    public static class ActivityPausedMessage implements Internal {}
    public static class ActivityResumedMessage implements Internal {}
    public static class ActivityDestroyedMessage implements Internal {}

    private interface Internal {}
}
