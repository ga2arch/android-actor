package com.gabriele.actor.internals;

import com.gabriele.actor.utils.Completable;

public class AskPattern {

    public static AskContext create(ActorContext context) {
        Completable completable = new Completable();
        ActorRef tempRef = context.actorOf(Props.create(AskActor.class, completable));

        return new AskContext(tempRef, completable);
    }

    public static class AskContext {
        private final ActorRef sender;
        private final Completable completable;

        public AskContext(ActorRef sender, Completable completable) {
            this.sender = sender;
            this.completable = completable;
        }

        public ActorRef getSender() {
            return sender;
        }

        public Completable getCompletable() {
            return completable;
        }
    }
}
