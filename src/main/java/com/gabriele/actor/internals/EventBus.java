package com.gabriele.actor.internals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {

    public final static String LOG_TAG = "EventBus";

    private final Context context;
    private ActorRef listener;
    private final ConcurrentHashMap<Class<?>, Set<ActorRef>> clazzBindings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActorRef, Set<Class<?>>> actorBindings = new ConcurrentHashMap<>();

    public EventBus(Context context) {
        this.context = context;
    }

    public void pipeTo(ActorRef ref) {
        listener = ref;
    }

    public void subscribe(Class clazz, ActorRef ref) {
        Set<ActorRef> refs = clazzBindings.get(clazz);
        if (refs != null) {
            refs.add(ref);
        } else {
            refs = Collections.newSetFromMap(new ConcurrentHashMap<ActorRef, Boolean>());
            refs.add(ref);
            clazzBindings.put(clazz, refs);
        }

        Set<Class<?>> clazzs = actorBindings.get(ref);
        if (clazzs != null) {
            clazzs.add(clazz);
        } else {
            clazzs = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());
            clazzs.add(clazz);
            actorBindings.put(ref, clazzs);
        }

    }

    public void subscribe(String uri, final ActorRef ref) {
        IntentFilter filter = new IntentFilter(uri);
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ref.tell(intent, null);
            }
        };
        getContext().registerReceiver(mReceiver, filter);
    }

    public void publish(Object obj, ActorRef sender) {
        Log.d(LOG_TAG, "Publishing: " + obj);

        Class clazz = obj.getClass();
        while (clazz != null) {
            Set<ActorRef> refs = clazzBindings.get(clazz);
            if (refs != null) {
                for (ActorRef ref: refs) {
                    try {
                        ref.tell(obj, sender);
                    } catch (ActorIsTerminatedException e) {
                        unsubscribe(ref);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public void unsubscribe(ActorRef ref) {
        Set<Class<?>> clazzs = actorBindings.get(ref);
        if (clazzs != null) {
            for (Class clazz: clazzs) {
                Set<ActorRef> refs = clazzBindings.get(clazz);
                if (refs != null) {
                    refs.remove(ref);

                    if (refs.isEmpty()) {
                        clazzBindings.remove(clazz);
                    }
                }
            }
        }
    }

    public void unsubscribe(Class clazz, ActorRef ref) {
        Set<ActorRef> clazzs = clazzBindings.get(clazz);
        if (clazzs != null) {
            clazzs.remove(ref);
            if (clazzs.isEmpty()) {
                clazzBindings.remove(clazz);

            }
        }
    }

    public HashSet<String> getSubscriptions() {
        HashSet<String> subs = new HashSet<>();
        for (Class<?> clazz: clazzBindings.keySet())
            subs.add(clazz.getSimpleName());

        return subs;
    }

    public Context getContext() {
        return context;
    }
}
