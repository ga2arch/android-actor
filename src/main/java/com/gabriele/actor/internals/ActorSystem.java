package com.gabriele.actor.internals;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

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
        if (!wakeLock.isHeld()) {
            wakeLock.acquire(TimeUnit.MINUTES.toMillis(5));
            Log.d(LOG_TAG, "Wakelock acquired");
        }
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
        actors.remove(actor);
        actor.getSelf().clear();
    }

    public ActorRef actorOf(ActorRef parent, Props props) {
        try {
            Constructor<?> constructor = props.getActorClazz().getConstructor(props.getClazzs());
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



    public Context getContext() {
        return context;
    }

    public ActorRef actorOf(ActorRef parent, Props props, Probe probe) {
        ActorRef ref = actorOf(parent, props);
        probes.put(ref, probe);
        return ref;
    }

    @Override
    public ActorRef actorOf(Props props) {
        return actorOf(null, props);
    }

    @Override
    public ActorRef actorOf(Props props, Probe probe) {
        ActorRef ref = actorOf(props);
        probes.put(ref, probe);
        return ref;
    }
}
