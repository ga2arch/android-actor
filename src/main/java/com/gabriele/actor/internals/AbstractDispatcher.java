package com.gabriele.actor.internals;

import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;
import de.jkeylockmanager.manager.LockCallback;

public abstract class AbstractDispatcher {
    protected ActorSystem system;
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
                        ConcurrentLinkedQueue<ActorMessage> mailbox = actorRef.get().getMailbox();
                        Iterator<ActorMessage> it = mailbox.iterator();
                        boolean terminated = false;
                        while (it.hasNext() && !terminated) {
                            ActorMessage message = it.next();
                            if (message.getObject() instanceof ActorMessage.PoisonPill) {
                                actor.afterStop();
                                getSystem().terminateActor(actorRef);
                                terminated = true;
                            }

                            ActorContext context = actor.getActorContext();
                            context.setSender(message.getSender());
                            context.setCurrentMessage(message);
                            Deque<OnReceiveFunction> stack = context.getStack();
                            if (stack.isEmpty())
                                actor.onReceive(message.getObject());
                            else
                                stack.getFirst().onReceive(message.getObject());

                            it.remove();
                        }
                        //if (terminated) throw new ActorIsTerminatedException();
                        running.remove(actorRef);
                        if (!terminated && mailbox.size() > 0) dispatch(actorRef);
                    }
                });
            }
        });
    }

    public ActorSystem getSystem() {
        return system;
    }

    public void setSystem(ActorSystem system) {
        this.system = system;
    }
}
