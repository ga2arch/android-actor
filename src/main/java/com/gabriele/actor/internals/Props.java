package com.gabriele.actor.internals;

import android.app.Activity;

import java.util.ArrayList;

public class Props {

    private AbstractDispatcher dispatcher = new ForkJoinDispatcher();
    private ArrayList<Class<?>> clazzs = new ArrayList<>();
    private Object[] args;

    public Props() { this.args = new Object[]{}; }

    public Props(Activity activity) {
        this.args = new Object[]{activity};
        this.clazzs.add(Activity.class);
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
