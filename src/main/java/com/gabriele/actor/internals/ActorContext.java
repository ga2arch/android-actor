package com.gabriele.actor.internals;

import android.content.Context;

import com.gabriele.actor.interfaces.ActorCreator;
import com.gabriele.actor.interfaces.OnReceiveFunction;
import com.gabriele.actor.testing.Probe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActorContext implements ActorCreator {

    private ActorSystem system;
    private ActorRef sender;
    private ActorRef parent;
    private final Set<ActorRef> children = Collections.newSetFromMap(new ConcurrentHashMap<ActorRef, Boolean>());
    private ActorRef self;
    private ActorMessage currentMessage;
    private AbstractDispatcher dispatcher;
    private final List<ActorMessage> stash = new ArrayList<>();
    private final Deque<OnReceiveFunction> stack = new ArrayDeque<>();

    public ActorContext(ActorSystem system,
                        ActorRef parent,
                        ActorRef self,
                        AbstractDispatcher dispatcher) {
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
        // Don't stash control messages
        if (currentMessage.getObject() instanceof ActorMessage.ControlMessage) return;
        stash.add(currentMessage);
    }

    protected void unstashAll() {
        Iterator<ActorMessage> it = stash.iterator();
        while (it.hasNext()) {
            ActorMessage msg = it.next();
            it.remove();
            getSelf().tell(msg.getObject(), msg.getSender());
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

    public List<ActorMessage> getStash() {
        return stash;
    }

    public Context getContext() {
        return getSystem().getContext();
    }

    public void setParent(ActorRef parent) {
        this.parent = parent;
    }

    public ActorMessage getCurrentMessage() {
        return currentMessage;
    }

    public void addChild(ActorRef ref) {
        children.add(ref);
    }

    public Set<ActorRef> getChildren() {
        return children;
    }

    public void removeChild(ActorRef ref) {
        children.remove(ref);
    }

    @Override
    public ActorRef actorOf(Props props) {
        return getSystem().actorOf(getSelf(), props);
    }

    @Override
    public ActorRef actorOf(Props props, String name) {
        return getSystem().actorOf(getSelf(), props, name);
    }

    @Override
    public ActorRef actorOf(Props props, Probe probe) {
        return getSystem().actorOf(getSelf(), props, probe);
    }

    @Override
    public ActorRef actorOf(Props props, String name, Probe probe) {
        return getSystem().actorOf(getSelf(), props, name, probe);
    }
}
