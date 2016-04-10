package com.gabriele.actor.internals;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {

    ActorRef listener;

    ConcurrentHashMap<Class<?>, Set<ActorRef>> clazzBindings = new ConcurrentHashMap<>();
    ConcurrentHashMap<ActorRef, Set<Class<?>>> actorBindings = new ConcurrentHashMap<>();

    public void pipeTo(ActorRef ref) {
        listener = ref;
    }

    public void subscribe(Class clazz, ActorRef ref) {
        if (clazzBindings.containsKey(clazz)) {
            clazzBindings.get(clazz).add(ref);
        } else {
            Set<ActorRef> refs = Collections.newSetFromMap(new ConcurrentHashMap<ActorRef, Boolean>());
            refs.add(ref);
            clazzBindings.put(clazz, refs);
        }

        if (actorBindings.containsKey(ref)) {
            actorBindings.get(ref).add(clazz);
        } else {
            Set<Class<?>> clazzs = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());
            clazzs.add(clazz);
            actorBindings.put(ref, clazzs);
        }

    }

    public void publish(Object obj, ActorRef sender) {
        Class clazz = obj.getClass();
        while (clazz != null) {
            if (clazzBindings.containsKey(clazz)) {
                Set<ActorRef> refs = clazzBindings.get(clazz);
                for (ActorRef ref: refs) {
                    ref.tell(obj, sender);
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
}
