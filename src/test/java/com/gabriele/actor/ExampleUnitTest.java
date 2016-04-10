package com.gabriele.actor;

import com.gabriele.actor.internals.Actor;
import com.gabriele.actor.internals.ActorRef;
import com.gabriele.actor.internals.ActorSystem;
import com.gabriele.actor.testing.Probe;

import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void testActorMessage() {
        ActorSystem system = new ActorSystem();
        Probe probe = new Probe();
        ActorRef ref = system.actorOf(Actor1.class, probe);
        ref.tell("prova", null);
        probe.expectMessage("prova", 1000);
    }

    public static class Actor1 extends Actor {

        @Override
        public void onReceive(Object message) {

        }
    }
}