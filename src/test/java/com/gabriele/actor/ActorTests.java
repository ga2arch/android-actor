package com.gabriele.actor;

import android.test.suitebuilder.annotation.LargeTest;

import com.gabriele.actor.internals.Actor;
import com.gabriele.actor.internals.ActorRef;
import com.gabriele.actor.internals.ActorSystem;
import com.gabriele.actor.internals.EventBus;
import com.gabriele.actor.testing.Probe;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@LargeTest
public class ActorTests {

    private static ActorSystem system;
    private static EventBus eventBus;

    @BeforeClass
    public static void setup() {
        system = new ActorSystem();
        eventBus = system.getEventBus();
    }

    @AfterClass
    public static void tearDown() {
        system = null;
        eventBus = null;
    }

    @Test
    public void testActorMessage() {
        Probe probe = new Probe();
        ActorRef ref = system.actorOf(Actor1.class, probe);
        ref.tell("test", null);
        probe.expectMessage("test", 1000);
    }

    @Test
    public void testEventBus() {
        Probe probe = new Probe();
        ActorRef ref = system.actorOf(Actor1.class, probe);
        eventBus.subscribe(Event.class, ref);

        Event event = new Event();
        eventBus.publish(event, null);
        probe.expectMessage(event, 1000);
    }

    @Test
    public void testActorAnswer() {
        Probe probe1 = new Probe();
        Probe probe2 = new Probe();

        ActorRef ref1 = system.actorOf(Actor1.class, probe1);
        ActorRef ref2 = system.actorOf(Actor2.class, probe2);

        ref2.tell("hello", ref1);
        probe2.expectMessage("hello", 1000);
        probe1.expectMessage("bonjour", 1000);
    }

    public static class Actor1 extends Actor {

        @Override
        public void onReceive(Object message) {

        }
    }

    public static class Actor2 extends Actor {

        @Override
        public void onReceive(Object message) {
            if (message == "hello")
                getSender().tell("bonjour", getSelf());
        }
    }

    public static class Event {

    }
}