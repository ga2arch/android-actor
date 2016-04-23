package com.gabriele.actor;

import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.LargeTest;

import com.gabriele.actor.internals.AbstractActor;
import com.gabriele.actor.internals.ActorRef;
import com.gabriele.actor.internals.ActorSystem;
import com.gabriele.actor.eventbus.EventBus;
import com.gabriele.actor.interfaces.OnReceiveFunction;
import com.gabriele.actor.internals.Props;
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
        system = new ActorSystem(new MockContext());
        eventBus = system.getEventBus();
    }

    @AfterClass
    public static void tearDown() {
        system = null;
        eventBus = null;
    }

    @Test
    public void testActorMessage() {
        Probe probe = new Probe(false);
        ActorRef ref = system.actorOf(Props.create(Actor1.class), probe);
        ref.tell("test", null);
        probe.expectMessage("test", 10);
    }

    @Test
    public void testEventBus() {
        Probe probe = new Probe();
        ActorRef ref = system.actorOf(Props.create(Actor1.class), probe);
        eventBus.subscribe(Event.class, ref);

        Event event = new Event();
        eventBus.publish(event, null);
        probe.expectMessage(event, 10);
    }

    @Test
    public void testActorAnswer() {
        Probe probe1 = new Probe();
        Probe probe2 = new Probe();

        ActorRef ref1 = system.actorOf(Props.create(Actor1.class), probe1);
        ActorRef ref2 = system.actorOf(Props.create(Actor2.class), probe2);

        ref2.tell("hello", ref1);
        probe2.expectMessage("hello", 10);
        probe1.expectMessage("bonjour", 10);
    }

    @Test
    public void testBecome() {
        Probe probe = new Probe();
        ActorRef ref1 = system.actorOf(Props.create(Actor1.class));
        ActorRef ref2 = system.actorOf(Props.create(Actor2.class), probe);

        ref1.tell(new BecomeEcho(), null);
        ref1.tell("ciao", ref2);

        probe.expectMessage("ciao", 1000);
    }

    @Test
    public void testStash() {
        Probe probe = new Probe();
        ActorRef ref1 = system.actorOf(Props.create(Actor1.class));
        ActorRef ref2 = system.actorOf(Props.create(Actor2.class), probe);

        ref1.tell("ciao", ref2);
        ref1.tell(new BecomeEcho(), null);

        probe.expectMessage("ciao", 1000);
    }

    public static class Actor1 extends AbstractActor {

        @Override
        public void onReceive(Object o) {
            if (o instanceof BecomeEcho) {
                getActorContext().become(echo);
                unstashAll();
            }
            else
                stash();
        }

        OnReceiveFunction echo = new OnReceiveFunction() {
            @Override
            public void onReceive(Object o) {
                getSender().tell(o, getSelf());
            }
        };
    }

    public static class Actor2 extends AbstractActor {

        @Override
        public void onReceive(Object o) {
            if (o == "hello")
                getSender().tell("bonjour", getSelf());
        }
    }

    public static class Event {

    }

    public static class BecomeEcho {}
}