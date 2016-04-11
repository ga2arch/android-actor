package com.gabriele.actor.internals;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;
import de.jkeylockmanager.manager.LockCallback;

public abstract class AbstractDispatcher {
    protected final KeyLockManager lockManager = KeyLockManagers.newLock();
    protected final Set<ActorRef> running = Collections.newSetFromMap(new ConcurrentHashMap<ActorRef, Boolean>());

    protected abstract ExecutorService getExecutorService();

    public void dispatch(final ActorRef actorRef) {
        lockManager.executeLocked(String.valueOf(actorRef.hashCode()), new LockCallback() {
            @Override
            public void doInLock() {
                if (running.contains(actorRef)) return;
                running.add(actorRef);

                getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        AbstractActor actor = actorRef.get();
                        if (actor == null) return;

                        ConcurrentLinkedQueue<Message> mailbox = actor.getMailbox();
                        Iterator<Message> it = mailbox.iterator();
                        while (it.hasNext()) {
                            Message message = it.next();
                            actor.setSender(message.getSender());
                            actor.onReceive(message.getObject());
                            it.remove();
                        }
                        running.remove(actorRef);
                        if (mailbox.size() > 0) dispatch(actorRef);
                    }
                });
            }
        });
    }
}
