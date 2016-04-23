package com.gabriele.actor.internals;

import android.util.Log;

import com.gabriele.actor.exceptions.ActorIsTerminatedException;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class AbstractDispatcher {
    private static final String LOG_TAG = "Dispatcher";

    private ActorSystem system;
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<ActorRef, Semaphore> running = new ConcurrentHashMap<>();

    protected abstract ExecutorService getExecutorService();
    public abstract Queue<ActorMessage> getMailbox();

    public void dispatch(final ActorRef actorRef) {
        try {
            final AbstractActor actor = actorRef.get();
            synchronized (actor) {
                synchronized (running) {
                    running.putIfAbsent(actorRef, new Semaphore(1));
                    final Semaphore semaphore = running.get(actorRef);
                    if (semaphore.availablePermits() == 0) return;
                    semaphore.tryAcquire();

                    getExecutorService().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                actor.receive();

                            } catch (ActorIsTerminatedException e) {
                                Log.e(LOG_TAG, e.getMessage(), e);

                            } catch (Exception e) {
                                Log.e(LOG_TAG, e.getMessage(), e);

                            } finally {
                                semaphore.release();
                                if (!actor.isTerminated() && actor.getMailbox().size() > 0) {
                                    running.remove(actorRef);
                                    dispatch(actorRef);
                                } else {
                                    running.remove(actorRef);
                                    // Release global wakelock is there are no running actor in 10 seconds.
                                    if (running.isEmpty())
                                        service.schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (running.isEmpty())
                                                    getSystem().releaseWakeLock();
                                            }
                                        }, 10, TimeUnit.SECONDS);
                                }
                            }
                        }
                    });
                }
            }
        } catch (ActorIsTerminatedException e) {
            Log.d(LOG_TAG, e.getMessage(), e);
        }
    }

    public ActorSystem getSystem() {
        return system;
    }

    public void setSystem(ActorSystem system) {
        this.system = system;
    }
}
