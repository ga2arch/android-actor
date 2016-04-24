package com.gabriele.actor.internals;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.gabriele.actor.eventbus.EventBus;
import com.gabriele.actor.exceptions.ActorIsTerminatedException;
import com.gabriele.actor.interfaces.ActorCreator;
import com.gabriele.actor.testing.Probe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ActorSystem implements ActorCreator {

    public static final String LOG_TAG = "ActorSystem";

    private final Context context;
    private final EventBus eventBus;
    private final Map<String, AbstractActor> actors = new ConcurrentHashMap<>();
    private final Map<ActorRef, Probe> probes = new ConcurrentHashMap<>();
    private final PowerManager.WakeLock wakeLock;

    public ActorSystem(Context context) {
        this.context = context;
        this.eventBus = new EventBus(this);
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ActorSystem");
        wakeLock.setReferenceCounted(false);
    }

    public void terminate() {
        for (AbstractActor actor: actors.values()) {
            actor.getSelf().tell(new ActorMessage.PoisonPill(), ActorRef.noSender());
        }
    }

    public synchronized void acquireWakeLock() {
        if (!wakeLock.isHeld()) {
            wakeLock.acquire(TimeUnit.MINUTES.toMillis(5));
            Log.d(LOG_TAG, "Wakelock acquired");
        }
    }

    public synchronized void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
            Log.d(LOG_TAG, "Wakelock released");
        }
    }

    public void publish(ActorRef actorRef, Object message, ActorRef sender) {
        AbstractActor actor = actors.get(actorRef.getPath());
        actor.getMailbox().add(new ActorMessage(message, sender));
        synchronized (probes) {
            Probe probe = probes.get(actorRef);
            if (probe != null) {
                probe.setMessage(message);
                probe.setSender(sender);
            }
            if (probe != null && !probe.toPropagate())
                return;
        }
        actor.getActorContext().getDispatcher().dispatch(actorRef);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void terminateActor(ActorRef actorRef) {
        AbstractActor actor = actors.get(actorRef.getPath());
        if (actor == null)
            throw new ActorIsTerminatedException();

        actors.remove(actorRef.getPath());
    }

    public ActorRef actorSelection(String path) {
        AbstractActor actor = actors.get(path);
        if (actor != null)
            return actor.getSelf();
        else
            return null;
    }

    public ActorRef actorOf(ActorRef parent, Props props) {
        return actorOf(parent, props, UUID.randomUUID().toString());
    }

    public ActorRef actorOf(ActorRef parent, Props props, String name) {
        try {
            Constructor<?> constructor = props.getActorClazz().getConstructor(props.getClazzs());
            AbstractActor actor = (AbstractActor) constructor.newInstance(props.getArgs());
            AbstractDispatcher dispatcher = props.getDispatcher();
            actor.setMailbox(dispatcher.getMailbox());

            String path;
            if (parent != null)
                path = String.format("%s/%s", parent.getPath(), name);
            else
                path = "//" + name;

            ActorRef self = new ActorRef(path, name);
            self.setSystem(this);
            dispatcher.setSystem(this);

            ActorContext actorContext = new ActorContext(this, parent, self, dispatcher);
            actors.put(self.getPath(), actor);

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

    public ActorRef actorOf(ActorRef parent, Props props, String name, Probe probe) {
        ActorRef ref = actorOf(parent, props, name);
        probes.put(ref, probe);
        return ref;
    }

    public ActorRef actorOf(ActorRef parent, Props props, Probe probe) {
        return actorOf(parent, props, UUID.randomUUID().toString(), probe);
    }

    public AbstractActor getActor(ActorRef ref) {
        return actors.get(ref.getPath());
    }

    @Override
    public ActorRef actorOf(Props props) {
        return actorOf(null, props);
    }

    @Override
    public ActorRef actorOf(Props props, String name) {
        return actorOf(null, props, name);
    }

    @Override
    public ActorRef actorOf(Props props, Probe probe) {
        ActorRef ref = actorOf(props);
        probes.put(ref, probe);
        return ref;
    }

    @Override
    public ActorRef actorOf(Props props, String name, Probe probe) {
        return null;
    }
}
