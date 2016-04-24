package com.gabriele.actor.interfaces;

import com.gabriele.actor.internals.ActorRef;
import com.gabriele.actor.internals.Props;
import com.gabriele.actor.testing.Probe;

public interface ActorCreator {

    ActorRef actorOf(Props props);
    ActorRef actorOf(Props props, String name);

    ActorRef actorOf(Props props, Probe probe);
    ActorRef actorOf(Props props, String name, Probe probe);

}
