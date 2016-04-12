package com.gabriele.actor.internals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractActor implements ActorInterface {

    private ActorSystem system;
    private ActorRef sender;
    private ActorRef self;
    private AbstractDispatcher dispatcher;
    private final ConcurrentLinkedQueue<Message> mailbox = new ConcurrentLinkedQueue<>();
    private final ArrayList<Message> stash = new ArrayList<>();
    private final Deque<OnReceiveFunction> stack = new ArrayDeque<>();

    public void preStart() {

    }

    public void afterStop() {

    }

    protected void become(OnReceiveFunction function) {
        stack.push(function);
    }

    protected void unbecome() {
        stack.pop();
    }

    protected void stash(Object object, ActorRef sender) {
        stash.add(new Message(object, sender));
    }

    protected void unstashAll() {
        Iterator<Message> it = stash.iterator();
        while (it.hasNext()) {
            Message msg = it.next();
            mailbox.add(msg);
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

    public ConcurrentLinkedQueue<Message> getMailbox() {
        return mailbox;
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
}
