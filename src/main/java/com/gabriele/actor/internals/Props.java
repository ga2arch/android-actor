package com.gabriele.actor.internals;

public class Props {

    private AbstractDispatcher dispatcher = new ForkJoinDispatcher();
    private Object[] args;

    public Props() { this.args = new Object[]{}; }
    public Props(Object... args) {
        this.args = args;
    }

    public Props withDispatcher(AbstractDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        return this;
    }

    public AbstractDispatcher getDispatcher() {
        return dispatcher;
    }

    public Object[] getArgs() {
        return args;
    }
}
