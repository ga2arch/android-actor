package com.gabriele.actor.internals;

import com.gabriele.actor.testing.Probe;

public interface ActorCreator {

    ActorRef actorOf(Class<? extends AbstractActor> actorClass);
    ActorRef actorOf(Class<? extends AbstractActor> actorClass, Props props);
    ActorRef actorOf(Class<? extends AbstractActor> actorClass, Probe probe, Props props);
    ActorRef actorOf(Class<? extends AbstractActor> actorClass, Probe probe);

}
