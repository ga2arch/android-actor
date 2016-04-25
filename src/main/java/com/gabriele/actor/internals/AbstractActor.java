package com.gabriele.actor.internals;

import android.content.Context;

import com.gabriele.actor.eventbus.EventBus;
import com.gabriele.actor.exceptions.ActorIsTerminatedException;
import com.gabriele.actor.interfaces.OnReceiveFunction;
import com.gabriele.actor.interfaces.WithReceive;

import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

public abstract class AbstractActor implements WithReceive {

    private ActorContext context;
    private boolean started = false;
    private boolean terminated = false;
    private Queue<ActorMessage> mailbox;

    /**
     * Executed in the thread of the dispatcher of the parent
     */
    public void onCreate() {

    }

    /**
     * Executed in the dispatcher
     */
    public void preStart() {

    }

    /**
     * Executed in the dispatcher
     */
    public void postStop() {

    }

    void receive() throws Exception {
        if (isTerminated())
            throw new ActorIsTerminatedException();

        if (!isStarted()) {
            preStart();
            setStarted();
        }

        Iterator<ActorMessage> it = getMailbox().iterator();
        while (it.hasNext() && !isTerminated()) {
            ActorMessage message = it.next();
            it.remove();

            ActorContext context = getActorContext();
            context.setSender(message.getSender());
            context.setCurrentMessage(message);
            Deque<OnReceiveFunction> stack = context.getStack();

            try {
                if (stack.isEmpty()) {
                    onReceive(message.getObject());

                } else {
                    stack.getFirst().onReceive(message.getObject());
                }

                if (message.getObject() instanceof ActorMessage.PoisonPill) {
                    terminate();
                }

            } catch (SecurityException e) {
                getSelf().tell(e, ActorRef.noSender());

            } catch (Exception e) {
                throw e;
            }
        }
    }

    protected void stopSelf() {
        getSystem().publish(getSelf(), new ActorMessage.PoisonPill(), getSelf());
    }

    private void terminate() {
        postStop();
        getEventBus().unsubscribe(getSelf());
        getMailbox().clear();
        setTerminated();
        if (getActorContext().getParent() != null) {
            getActorContext().getParent().tell(new ActorMessage.Terminated(), getSelf());
        }
        getSystem().terminateActor(getSelf());
    }

    private void restart() {
        preStart();
    }

    public Queue<ActorMessage> getMailbox() {
        return mailbox;
    }

    public ActorContext getActorContext() {
        return context;
    }

    public void setActorContext(ActorContext context) {
        this.context = context;
    }

    public ActorRef getSender() {
        return getActorContext().getSender();
    }

    public ActorRef getSelf() {
        return getActorContext().getSelf();
    }

    public ActorSystem getSystem() {
        return getActorContext().getSystem();
    }

    public void stash() {
        getActorContext().stash();
    }

    public void unstashAll() {
        getActorContext().unstashAll();
    }

    public void become(OnReceiveFunction fun) {
        getActorContext().become(fun);
    }

    public void unbecome() {
        getActorContext().unbecome();
    }

    public EventBus getEventBus() {
        return getSystem().getEventBus();
    }

    public Context getContext() {
        return getActorContext().getContext();
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void setTerminated() {
        this.terminated = true;
        getSelf().setTerminated();
    }

    public void setStarted() {
        this.started = true;
    }

    public void setMailbox(Queue<ActorMessage> mailbox) {
        this.mailbox = mailbox;
    }
}

