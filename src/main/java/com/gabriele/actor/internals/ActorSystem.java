package com.gabriele.actor.internals;

import com.gabriele.actor.testing.Probe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ActorSystem {

    Dispatcher dispatcher = new Dispatcher();

    public ActorRef actorOf(Class<? extends Actor> actorClass, Probe probe) {
        ActorRef ref = actorOf(actorClass);
        dispatcher.addProbe(ref, probe);
        probe.setReceiver(ref);
        return ref;
    }

    public ActorRef actorOf(Class<? extends Actor> actorClass) {
        try {
            Constructor<?> constructor = actorClass.getConstructor();
            Actor actor = (Actor) constructor.newInstance();
            dispatcher.addActor(actor);
            ActorRef ref = new ActorRef(this, actor);
            actor.setSelf(ref);
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

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}
