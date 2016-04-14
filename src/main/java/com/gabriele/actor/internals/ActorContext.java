package com.gabriele.actor.internals;

import android.content.Context;

import com.gabriele.actor.testing.Probe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;

public class ActorContext implements ActorCreator {

    private ActorSystem system;
    private ActorRef sender;
    private ActorRef parent;
    private ActorRef self;
    private ActorMessage currentMessage;
    private AbstractDispatcher dispatcher;
    private final ArrayList<ActorMessage> stash = new ArrayList<>();
    private final Deque<OnReceiveFunction> stack = new ArrayDeque<>();

    public ActorContext(ActorSystem system, ActorRef parent, ActorRef self, AbstractDispatcher dispatcher) {
        this.system = system;
        this.parent = parent;
        this.self = self;
        this.dispatcher = dispatcher;
    }

    public void become(OnReceiveFunction function) {
        stack.push(function);
    }

    public void unbecome() {
        stack.pop();
    }

    public void stash() {
        stash.add(currentMessage);
    }

    protected void unstashAll() {
        Iterator<ActorMessage> it = stash.iterator();
        while (it.hasNext()) {
            ActorMessage msg = it.next();
            getSelf().tell(msg.getObject(), msg.getSender());
            it.remove();
        }
    }

    void setSender(ActorRef sender) {
        this.sender = sender;
    }

    public ActorRef getSelf() {
        return self;
    }

    public void setSelf(ActorRef self) {
        this.self = self;
    }

    public ActorRef getSender() {
        return sender;
    }

    public AbstractDispatcher getDispatcher() {
        return dispatcher;
    }

    public ActorSystem getSystem() {
        return system;
    }

    public void setSystem(ActorSystem system) {
        this.system = system;
    }

    public void setDispatcher(AbstractDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Deque<OnReceiveFunction> getStack() {
        return stack;
    }

    public void setCurrentMessage(ActorMessage currentMessage) {
        this.currentMessage = currentMessage;
    }

    public ActorRef getParent() {
        return parent;
    }

    public ArrayList<ActorMessage> getStash() {
        return stash;
    }

    public Context getContext() {
        return getSystem().getContext();
    }

    public void setParent(ActorRef parent) {
        this.parent = parent;
    }

    @Override
    public ActorRef actorOf(Class<? extends AbstractActor> actorClass) {
        return getSystem().actorOf(self, actorClass);
    }

    @Override
    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Props props) {
        return getSystem().actorOf(self, actorClass, props);
    }

    @Override
    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Probe probe, Props props) {
        return getSystem().actorOf(self, actorClass, probe, props);
    }

    @Override
    public ActorRef actorOf(Class<? extends AbstractActor> actorClass, Probe probe) {
        return getSystem().actorOf(self, actorClass, probe);
    }
}
