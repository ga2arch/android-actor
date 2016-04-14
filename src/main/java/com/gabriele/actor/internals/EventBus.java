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
    private final int REPLAY_SIZE = 5;

    private final Context context;
    private final ConcurrentHashMap<Class<?>, Set<ActorRef>> classToActors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActorRef, Set<Class<?>>> actorToClass = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BroadcastReceiver> uriToReceiver = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActorRef, Set<String>> actorToReceivers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Set<ActorRef>> publishers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, ActorMessage> sticky = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, EvictingQueue<ActorMessage>> replay = new ConcurrentHashMap<>();

    public EventBus(Context context) {
        this.context = context;
    }

    public synchronized void subscribe(Class clazz, ActorRef ref) {
        Set<ActorRef> refs = classToActors.get(clazz);
        if (refs != null) {
            refs.add(ref);
        } else {
            refs = Collections.newSetFromMap(new ConcurrentHashMap<ActorRef, Boolean>());
            refs.add(ref);
            classToActors.put(clazz, refs);
        }

        Set<Class<?>> clazzs = actorToClass.get(ref);
        if (clazzs != null) {
            clazzs.add(clazz);
        } else {
            clazzs = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());
            clazzs.add(clazz);
            actorToClass.put(ref, clazzs);
        }

        Set<ActorRef> pubs = publishers.get(clazz);
        if (pubs != null) {
            for (ActorRef pub: pubs)
                pub.tell(new ActivateMessage(clazz), ref);
        }

        EvictingQueue<ActorMessage> replayQueue = replay.get(clazz);
        if (replayQueue != null) {
            while (replayQueue.size() > 0) {
                ActorMessage message = replayQueue.pop();
                ref.tell(message.getObject(), message.getSender());
            }
        }

        ActorMessage o = sticky.get(clazz);
        if (o != null)
            ref.tell(o.getObject(), o.getSender());

        if (clazz != SubscribeMessage.class)
            publish(new SubscribeMessage(clazz), ref);
    }

    public synchronized void subscribe(String uri, final ActorRef ref) {
        IntentFilter filter = new IntentFilter(uri);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ref.tell(intent, null);
            }
        };
        getContext().registerReceiver(receiver, filter);
        uriToReceiver.put(uri, receiver);

        Set<String> receivers = actorToReceivers.get(ref);
        if (receivers != null) {
            receivers.add(uri);
        } else {
            receivers = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            receivers.add(uri);
            actorToReceivers.put(ref, receivers);
        }
    }

    public synchronized void publish(Object obj, ActorRef sender) {
        publish(obj, sender, false);
    }

    public synchronized void publish(Object obj, ActorRef sender, boolean isSticky) {
        Log.d(LOG_TAG, "Publishing: " + obj);

        Class clazz = obj.getClass();
        if (isSticky) {
            sticky.put(clazz, new ActorMessage(obj, sender));
        }

        boolean sent = false;
        while (clazz != null) {
            Set<ActorRef> refs = classToActors.get(clazz);
            if (refs != null) {
                for (ActorRef ref: refs) {
                    try {
                        ref.tell(obj, sender);
                        sent = true;
                    } catch (ActorIsTerminatedException e) {
                        unsubscribe(ref);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        if (!sent) {
            EvictingQueue<ActorMessage> replayQueue = replay.get(obj.getClass());
            if (replayQueue == null) {
                replayQueue = new EvictingQueue<>(REPLAY_SIZE);
                replay.put(obj.getClass(), replayQueue);
            }
            replayQueue.add(new ActorMessage(obj, sender));
        }
    }

    public synchronized void unsubscribe(ActorRef ref) {
        Set<Class<?>> clazzs = actorToClass.get(ref);
        if (clazzs != null) {
            for (Class clazz: clazzs) {
                Set<ActorRef> refs = classToActors.get(clazz);
                if (refs != null) {
                    refs.remove(ref);

                    if (refs.isEmpty()) {
                        classToActors.remove(clazz);
                        replay.remove(clazz);

                        Set<ActorRef> pubs = publishers.get(clazz);
                        if (pubs != null) {
                            for (ActorRef pub: pubs)
                                pub.tell(new DeactivateMessage(clazz), ref);
                        }

                        if (clazz != UnsubscribeMessage.class)
                            publish(new UnsubscribeMessage(clazz), ref);
                    }
                }
            }
        }

        Set<String> receivers = actorToReceivers.get(ref);
        if (receivers != null) {
            for (String uri: receivers) {
                BroadcastReceiver receiver = uriToReceiver.get(uri);
                if (receiver != null) {
                    getContext().unregisterReceiver(receiver);
                }
            }
            actorToReceivers.remove(ref);
        }
    }

    public synchronized void unsubscribe(Class clazz, ActorRef ref) {
        Set<ActorRef> clazzs = classToActors.get(clazz);
        if (clazzs != null) {
            clazzs.remove(ref);
            if (clazzs.isEmpty()) {
                classToActors.remove(clazz);
                replay.remove(clazz);

                Set<ActorRef> pubs = publishers.get(clazz);
                if (pubs != null) {
                    for (ActorRef pub: pubs)
                        pub.tell(new DeactivateMessage(clazz), ref);
                }
                if (clazz != UnsubscribeMessage.class)
                    publish(new UnsubscribeMessage(clazz), ref);
            }
        }
    }

    public synchronized void unsubscribe(String uri, ActorRef ref) {
        BroadcastReceiver receiver = uriToReceiver.get(uri);
        if (receiver != null) {
            getContext().unregisterReceiver(receiver);
            Set<String> receivers = actorToReceivers.get(ref);
            if (receivers != null)
                receivers.remove(uri);
        }
    }

    public synchronized HashSet<Class<?>> getSubscriptions() {
        HashSet<Class<?>> subs = new HashSet<>();
        for (Class<?> clazz: classToActors.keySet())
            subs.add(clazz);

        return subs;
    }

    public synchronized boolean isSubscribed(Class<?> clazz, ActorRef ref) {
        Set<ActorRef> actors = classToActors.get(clazz);
        if (actors != null) {
            return actors.contains(ref);
        }

        return false;
    }

    public synchronized void registerPublisher(Class<?> clazz, ActorRef ref) {
        Set<ActorRef> refs = publishers.get(clazz);
        if (refs != null) {
            refs.add(ref);
        } else {
            refs = Collections.newSetFromMap(new ConcurrentHashMap<ActorRef, Boolean>());
            refs.add(ref);
            publishers.put(clazz, refs);
        }
    }

    public synchronized void unregisterPublisher(Class<?> clazz, ActorRef ref) {
        Set<ActorRef> refs = publishers.get(clazz);
        if (refs != null) {
            refs.remove(ref);
        }
    }

    public Context getContext() {
        return context;
    }

    public static class SubscribeMessage {
        private Class<?> event;

        public SubscribeMessage(Class<?> event) {
            this.event = event;
        }

        public Class<?> getClazz() {
            return event;
        }
    }

    public static class UnsubscribeMessage {
        private Class<?> event;

        public UnsubscribeMessage(Class<?> event) {
            this.event = event;
        }

        public Class<?> getClazz() {
            return event;
        }
    }

    public static class ActivateMessage {
        private Class<?> event;

        public ActivateMessage(Class<?> event) {
            this.event = event;
        }

        public Class<?> getEvent() {
            return event;
        }
    }

    public static class DeactivateMessage {
        private Class<?> event;

        public DeactivateMessage(Class<?> event) {
            this.event = event;
        }

        public Class<?> getEvent() {
            return event;
        }
    }
}
