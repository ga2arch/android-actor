package com.gabriele.actor.internals;

import android.os.Build;

import com.gabriele.actor.testing.Probe;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;
import de.jkeylockmanager.manager.LockCallback;

public class Dispatcher {
    private final KeyLockManager lockManager = KeyLockManagers.newLock();
    private final ExecutorService executorService;
    private final ConcurrentHashMap<Actor, ConcurrentLinkedQueue<Message>> mailboxes = new ConcurrentHashMap<>();
    private final Set<ActorRef> running = Collections.newSetFromMap(new ConcurrentHashMap<ActorRef, Boolean>());
    private final ConcurrentHashMap<ActorRef, Probe> probes = new ConcurrentHashMap<>();

    public Dispatcher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            executorService = new ForkJoinPool();
        } else {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
    }

    public void addActor(Actor actor) {
        mailboxes.put(actor, new ConcurrentLinkedQueue<Message>());
    }

    public void addProbe(ActorRef ref, Probe probe) {
        probes.put(ref, probe);
    }

    public void publish(ActorRef actorRef, Object message, ActorRef sender) {
        ConcurrentLinkedQueue<Message> mailbox = mailboxes.get(actorRef.get());
        mailbox.add(new Message(message, sender));
        Probe probe = probes.get(actorRef);
        if (probe != null) {
            probe.setMessage(message);
            probe.setSender(sender);
        }
        schedule(actorRef);
    }

    private void schedule(final ActorRef actorRef) {
        lockManager.executeLocked(String.valueOf(actorRef.hashCode()), new LockCallback() {
            @Override
            public void doInLock() {
                if (running.contains(actorRef)) return;
                running.add(actorRef);

                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        ConcurrentLinkedQueue<Message> mailbox = mailboxes.get(actorRef.get());
                        Iterator<Message> it = mailbox.iterator();
                        while (it.hasNext()) {
                            Message message = it.next();
                            Actor actor = actorRef.get();
                            actor.setSender(message.getSender());
                            actor.onReceive(message.getObject());
                            it.remove();
                        }
                        running.remove(actorRef);
                        if (mailbox.size() > 0) schedule(actorRef);
                    }
                });
            }
        });
    }

    private class Message {
        Object object;
        ActorRef sender;

        public Message(Object object, ActorRef sender) {
            this.object = object;
            this.sender = sender;
        }

        public Object getObject() {
            return object;
        }

        public ActorRef getSender() {
            return sender;
        }
    }

}
