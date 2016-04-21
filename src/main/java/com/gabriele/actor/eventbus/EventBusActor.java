package com.gabriele.actor.eventbus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.gabriele.actor.exceptions.ActorIsTerminatedException;
import com.gabriele.actor.internals.AbstractActor;
import com.gabriele.actor.internals.ActorMessage;
import com.gabriele.actor.internals.ActorRef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EventBusActor extends AbstractActor {

    public final static String LOG_TAG = "EventBusActor";
    protected final int REPLAY_SIZE = 5;

    private final Map<Class<?>, Set<ActorRef>> classToActors = new HashMap<>();
    private final Map<ActorRef, Set<Class<?>>> actorToClass = new HashMap<>();
    private final Map<String, BroadcastReceiver> uriToReceiver = new HashMap<>();
    private final Map<ActorRef, Set<String>> actorToReceivers = new HashMap<>();
    private final Map<Class<?>, Set<ActorRef>> publishers = new HashMap<>();
    private final Map<Class<?>, ActorMessage> sticky = new HashMap<>();
    private final Map<Class<?>, EvictingQueue<ActorMessage>> replay = new HashMap<>();

    @Override
    public void onReceive(Object o) {
        if (o instanceof EventBus.SubscribeMessage) {
            EventBus.SubscribeMessage message = ((EventBus.SubscribeMessage) o);
            if (message.uri != null)
                subscribe(message.uri, message.ref);
            else
                subscribe(message.event, message.ref, message.silent);

        } else if (o instanceof EventBus.UnsubscribeMessage) {
            EventBus.UnsubscribeMessage message = ((EventBus.UnsubscribeMessage) o);
            if (message.uri != null)
                unsubscribe(message.uri, message.ref);
            else if (message.event != null)
                unsubscribe(message.event, message.ref);
            else
                unsubscribe(message.ref);

        } else if (o instanceof EventBus.PublishMessage) {
            EventBus.PublishMessage message = ((EventBus.PublishMessage) o);
            publish(message.object, message.sender, message.isSticky);

        } else if (o instanceof EventBus.RegisterPublisherMessage) {
            EventBus.RegisterPublisherMessage message = ((EventBus.RegisterPublisherMessage) o);
            registerPublisher(message.event, message.ref);

        } else if (o instanceof EventBus.UnregisterPublisherMessage) {
            EventBus.UnregisterPublisherMessage message = ((EventBus.UnregisterPublisherMessage) o);
            unregisterPublisher(message.event, message.ref);

        } else if (o instanceof EventBus.AskSubscriptionsMessage) {
            HashSet<Class<?>> subs = new HashSet<>();
            for (Class<?> clazz: classToActors.keySet())
                subs.add(clazz);

            getSender().tell(new EventBus.SubscriptionsMessage(subs), getSelf());
        }
    }

    protected void subscribe(Class clazz, ActorRef ref, boolean silent) {
        Set<ActorRef> refs = classToActors.get(clazz);
        if (refs != null) {
            refs.add(ref);
        } else {
            refs = new HashSet<>();
            refs.add(ref);
            classToActors.put(clazz, refs);
        }

        Set<Class<?>> clazzs = actorToClass.get(ref);
        if (clazzs != null) {
            clazzs.add(clazz);
        } else {
            clazzs = new HashSet<>();
            clazzs.add(clazz);
            actorToClass.put(ref, clazzs);
        }

        Set<ActorRef> pubs = publishers.get(clazz);
        if (pubs != null) {
            for (ActorRef pub: pubs)
                pub.tell(new EventBus.ActivateMessage(clazz), ref);
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

        if (!silent)
            publish(new EventBus.SubscribedMessage(clazz), ref);
    }

    protected void subscribe(String uri, final ActorRef ref) {
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
            receivers = new HashSet<>();
            receivers.add(uri);
            actorToReceivers.put(ref, receivers);
        }
    }

    protected void unsubscribe(Class clazz, ActorRef ref) {
        Set<ActorRef> clazzs = classToActors.get(clazz);
        if (clazzs != null) {
            clazzs.remove(ref);
            if (clazz != EventBus.UnsubscribedMessage.class)
                publish(new EventBus.UnsubscribedMessage(clazz), ref);

            if (clazzs.isEmpty()) {
                classToActors.remove(clazz);
                replay.remove(clazz);

                Set<ActorRef> pubs = publishers.get(clazz);
                if (pubs != null) {
                    for (ActorRef pub: pubs)
                        pub.tell(new EventBus.DeactivateMessage(clazz), ref);
                }
            }
        }
    }

    protected void unsubscribe(String uri, ActorRef ref) {
        BroadcastReceiver receiver = uriToReceiver.get(uri);
        if (receiver != null) {
            getContext().unregisterReceiver(receiver);
            Set<String> receivers = actorToReceivers.get(ref);
            if (receivers != null)
                receivers.remove(uri);
        }
    }

    protected void unsubscribe(ActorRef ref) {
        Set<Class<?>> clazzs = actorToClass.get(ref);
        if (clazzs != null) {
            for (Class clazz : clazzs) {
                unsubscribe(clazz, ref);
            }
        }

        Set<String> receivers = actorToReceivers.get(ref);
        if (receivers != null) {
            for (String uri : receivers) {
                BroadcastReceiver receiver = uriToReceiver.get(uri);
                if (receiver != null) {
                    getContext().unregisterReceiver(receiver);
                }
            }
            actorToReceivers.remove(ref);
        }
    }

    public void publish(Object obj, ActorRef sender) {
        publish(obj, sender, false);
    }

    public void publish(Object obj, ActorRef sender, boolean isSticky) {
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

    public void registerPublisher(Class<?> clazz, ActorRef ref) {
        Set<ActorRef> refs = publishers.get(clazz);
        if (refs != null) {
            refs.add(ref);
        } else {
            refs = new HashSet<>();
            refs.add(ref);
            publishers.put(clazz, refs);
        }
    }

    public void unregisterPublisher(Class<?> clazz, ActorRef ref) {
        Set<ActorRef> refs = publishers.get(clazz);
        if (refs != null) {
            refs.remove(ref);
        }
    }
}
