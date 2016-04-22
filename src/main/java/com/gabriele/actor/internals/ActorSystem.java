package com.gabriele.actor.internals;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.gabriele.actor.dispatchers.ForkJoinDispatcher;
import com.gabriele.actor.eventbus.EventBus;
import com.gabriele.actor.interfaces.ActorCreator;
import com.gabriele.actor.testing.Probe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ActorSystem implements ActorCreator {

    public static final String LOG_TAG = "ActorSystem";

    private final Context context;
    private final EventBus eventBus;
    private final Set<AbstractActor> actors = Collections.newSetFromMap(new ConcurrentHashMap<AbstractActor, Boolean>());
    private final ConcurrentHashMap<ActorRef, Probe> probes = new ConcurrentHashMap<>();
    private final PowerManager.WakeLock wakeLock;

    public ActorSystem(Context context) {
        this.context = context;
        this.eventBus = new EventBus(this);
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ActorSystem");
        wakeLock.setReferenceCounted(false);
    }

    public void terminate() {
        for (AbstractActor actor: actors) {
            actor.getSelf().tell(new ActorMessage.PoisonPill(), ActorRef.noSender());
        }
    }

    public void acquireWakeLock() {
        wakeLock.acquire(TimeUnit.MINUTES.toMillis(5));
        Log.d(LOG_TAG, "Wakelock acquired");

    }

    public void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
            Log.d(LOG_TAG, "Wakelock released");
        }
    }

    public void publish(ActorRef actorRef, Object message, ActorRef sender) {
        AbstractActor actor = actorRef.get();
        actor.getMailbox().add(new ActorMessage(message, sender));
        Probe probe = probes.get(actorRef);
        if (probe != null) {
            probe.setMessage(message);
            probe.setSender(sender);
        }
        if (probe != null && !probe.toPropagate()) return;
        actor.getActorContext().getDispatcher().dispatch(actorRef);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void terminateActor(AbstractActor actor) {
        getEventBus().unsubscribe(actor.getSelf());
        actors.remove(actor);
        actor.getSelf().clear();
    }

    public ActorRef actorOf(ActorRef parent, Class<? extends AbstractActor> actorClass, Props props) {
        try {
            Constructor<?> constructor = actorClass.getConstructor(props.getClazzs());
            AbstractActor actor = (AbstractActor) constructor.newInstance(props.getArgs());
            AbstractDispatcher dispatcher = props.getDispatcher();
            actor.setMailbox(dispatcher.getMailbox());

            ActorRef self = new ActorRef(actor);
            self.setSystem(this);
            dispatcher.setSystem(this);
            actors.add(actor);

            ActorContext actorContext = new ActorContext(this, parent, self, dispatcher);
            actor.setActorContext(actorContext);
            actor.onCreate();
            dispatcher.dispatch(self);
            return self;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            e.getCause().printStackTrace();
        }

        return null;
    }

    public ActorRef actorOf(ActorRef parent, Class<? extends AbstractActor> actorClass, Probe probe, Props props) {
        ActorRef ref = actorOf(parent, actorClass, props);
        probes.put(ref, probe);
        probe.setReceiver(ref);
        return ref;
    }

    public ActorRef actorOf(ActorRef parent, Class<? extends AbstractActor> actorClass, Probe probe) {
        return actorOf(parent, actorClass, probe, new Props().withDispatcher(ForkJoinDispatcher.getInstance()));
    }

    public ActorRef actorOf(ActorRef parent, Class<? extends AbstractActor> actorClass) {
        return actorOf(parent, actorClass, new Props().withDispatcher(ForkJoinDispatcher.getInstance()));
    }

    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Probe probe) {
        return actorOf(null, actorClass, probe);
    }

    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Probe probe, Props props) {
        return actorOf(null, actorClass, probe, props);
    }

    public ActorRef actorOf(Class<? extends AbstractActor> actorClass) {
        return actorOf(actorClass, new Props().withDispatcher(ForkJoinDispatcher.getInstance()));
    }

    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Props props) {
        return actorOf(null, actorClass, props);
    }

    public Context getContext() {
        return context;
    }
}
