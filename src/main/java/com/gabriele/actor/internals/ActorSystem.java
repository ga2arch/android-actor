package com.gabriele.actor.internals;

import com.gabriele.actor.testing.Probe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActorSystem {

    private final EventBus eventBus = new EventBus();
    private final Set<AbstractActor> actors = Collections.newSetFromMap(new ConcurrentHashMap<AbstractActor, Boolean>());
    private final ConcurrentHashMap<ActorRef, Probe> probes = new ConcurrentHashMap<>();

    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Probe probe) {
        return actorOf(actorClass, probe, new Props().withDispatcher(new ForkJoinDispatcher()));
    }

    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Probe probe, Props props) {
        ActorRef ref = actorOf(actorClass, props);
        probes.put(ref, probe);
        probe.setReceiver(ref);
        return ref;
    }

    public ActorRef actorOf(Class<? extends AbstractActor> actorClass) {
        return actorOf(actorClass, new Props().withDispatcher(new ForkJoinDispatcher()));
    }

    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Props props) {
        try {
            Constructor<?> constructor = actorClass.getConstructor(props.getClazzs());
            AbstractActor actor = (AbstractActor) constructor.newInstance(props.getArgs());
            actor.setDispatcher(props.getDispatcher());
            actors.add(actor);

            ActorRef ref = new ActorRef(actor);
            actor.setSelf(ref);
            actor.setSystem(this);

            return ref;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void publish(ActorRef actorRef, Object message, ActorRef sender) {
        AbstractActor actor = actorRef.get();
        if (actor == null) return;

        actor.getMailbox().add(new Message(message, sender));
        Probe probe = probes.get(actorRef);
        if (probe != null) {
            probe.setMessage(message);
            probe.setSender(sender);
        }
        actor.getDispatcher().dispatch(actorRef);
    }

    public EventBus getEventBus() {
        return eventBus;
    }


}
