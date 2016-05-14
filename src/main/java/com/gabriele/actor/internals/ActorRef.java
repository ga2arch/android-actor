package com.gabriele.actor.internals;

public class ActorRef {

    private ActorSystem system;
    private final String path;
    private final String name;
    private volatile boolean isTerminated;

    public ActorRef(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public void setSystem(ActorSystem system) {
        this.system = system;
    }

    public void tell(final Object message, ActorRef sender) {
        if (sender == null)
            sender = system.getDeadLetter();

        if (message instanceof ActorMessage.PoisonPill) {
            setTerminated();
            system.publish(this, message, sender);
            return;
        }

        if (isTerminated())
            system.publish(system.getDeadLetter(), new ActorMessage.DeadLetter(message, this), sender);
        else
            system.publish(this, message, sender);
    }

    public static ActorRef noSender() {
        return null;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setTerminated() {
        isTerminated = true;
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    @Override
    public String toString() {
        return "ActorRef{" +
                "system=" + system +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", isTerminated=" + isTerminated +
                '}';
    }
}
