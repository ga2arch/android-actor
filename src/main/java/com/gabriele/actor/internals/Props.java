package com.gabriele.actor.internals;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class Props {

    private AbstractDispatcher dispatcher;
    private ArrayList<Class<?>> clazzs = new ArrayList<>();
    private Object[] args;

    public Props() { this.args = new Object[]{}; }

    public Props(Activity activity) {
        if (!(activity instanceof ActorInterface))
            throw new RuntimeException("Activity doesn't implement ActorInterface");

        this.args = new Object[]{activity};
        this.clazzs.add(Activity.class);
    }

    public Props(AppCompatActivity activity) {
        if (!(activity instanceof ActorInterface))
            throw new RuntimeException("AppCompatActivity doesn't implement ActorInterface");

        this.args = new Object[]{activity};
        this.clazzs.add(AppCompatActivity.class);
    }

    public Props(Context context) {
        this.args = new Object[]{context};
        this.clazzs.add(Context.class);
    }

    public Props(Object... args) {
        this.args = args;
        buildClazz();
    }

    public Props withDispatcher(AbstractDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        return this;
    }

    public AbstractDispatcher getDispatcher() {
        return dispatcher;
    }

    public Object[] getArgs() {
        return args;
    }

    private void buildClazz() {
        for (Object extra: getArgs()) {
            if (extra instanceof Activity)
                clazzs.add(Activity.class);
            else
                clazzs.add(extra.getClass());
        }
    }

    public Class<?>[] getClazzs() {
        return clazzs.toArray(new Class<?>[clazzs.size()]);
    }
}
