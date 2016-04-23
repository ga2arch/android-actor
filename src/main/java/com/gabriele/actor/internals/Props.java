package com.gabriele.actor.internals;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import com.gabriele.actor.android.ActivityActor;
import com.gabriele.actor.dispatchers.ForkJoinDispatcher;
import com.gabriele.actor.interfaces.WithReceive;

import java.util.ArrayList;
import java.util.Arrays;

public final class Props {

    private Class<? extends AbstractActor> actorClazz;
    private AbstractDispatcher dispatcher = ForkJoinDispatcher.getInstance();
    private final ArrayList<Class<?>> clazzs = new ArrayList<>();
    private Object[] args;

    public static Props create(Class<? extends AbstractActor> clazz) {
        return new Props(clazz);
    }

    public static Props create(Class<? extends AbstractActor> clazz, Object... args) {
        return new Props(clazz, args);
    }

    public static Props create(Activity activity) {
        return new Props(activity);
    }

    public static Props create(AppCompatActivity activity) {
        return new Props(activity);
    }

    private Props(Class<? extends AbstractActor> actorClazz) {
        this.actorClazz = actorClazz;
    }

    private Props(Class<? extends AbstractActor> actorClazz, Object... args) {
        this.actorClazz = actorClazz;
        this.args  = args;
        buildClazz();
    }

    private Props(Activity activity) {
        if (!(activity instanceof WithReceive))
            throw new RuntimeException("Activity doesn't implement ActorInterface");

        this.actorClazz = ActivityActor.class;
        this.args = new Object[]{activity};
        this.clazzs.add(Activity.class);
    }

    private Props(AppCompatActivity activity) {
        if (!(activity instanceof WithReceive))
            throw new RuntimeException("AppCompatActivity doesn't implement ActorInterface");

        this.actorClazz = ActivityActor.class;
        this.args = new Object[]{activity};
        this.clazzs.add(AppCompatActivity.class);
    }

    public Props withDispatcher(AbstractDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        return this;
    }

    public AbstractDispatcher getDispatcher() {
        return dispatcher;
    }

    public Class<? extends AbstractActor> getActorClazz() {
        return actorClazz;
    }

    public Object[] getArgs() {
        if (args == null)
            return new Object[]{};
        else
            return Arrays.copyOf(args, args.length);
    }

    private void buildClazz() {
        for (Object extra: getArgs()) {
            clazzs.add(extra.getClass());
        }
    }

    public Class<?>[] getClazzs() {
        return clazzs.toArray(new Class<?>[clazzs.size()]);
    }
}
