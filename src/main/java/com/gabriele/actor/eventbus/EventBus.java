package com.gabriele.actor.eventbus;

import com.gabriele.actor.internals.ActorRef;
import com.gabriele.actor.internals.ActorSystem;
import com.gabriele.actor.internals.Props;

import java.util.HashSet;

public class EventBus {

    private ActorSystem system;
    private ActorRef eventBusRef;

    public EventBus(ActorSystem system) {
        this.system = system;
        eventBusRef = system.actorOf(Props.create(EventBusActor.class));
    }

    public void subscribe(Class clazz, ActorRef ref) {
        eventBusRef.tell(new EventBus.SubscribeMessage(clazz, ref, false), ref);
    }

    public void subscribeSilent(Class clazz, ActorRef ref) {
        eventBusRef.tell(new EventBus.SubscribeMessage(clazz, ref, true), ref);
    }

    public void subscribe(String uri, final ActorRef ref) {
        eventBusRef.tell(new EventBus.SubscribeMessage(uri, ref), ref);
    }

    public void publish(Object obj, ActorRef sender) {
        publish(obj, sender, false);
    }

    public void publishSticky(Object obj, ActorRef sender) {
        publish(obj, sender, true);
    }

    public void publish(Object obj, ActorRef sender, boolean isSticky) {
        eventBusRef.tell(new EventBus.PublishMessage(obj, sender, isSticky), sender);
    }

    public void unsubscribe(ActorRef ref) {
        eventBusRef.tell(new EventBus.UnsubscribeMessage(ref), ref);
    }

    public void unsubscribe(Class clazz, ActorRef ref) {
        eventBusRef.tell(new EventBus.UnsubscribeMessage(clazz, ref), ref);
    }

    public void unsubscribe(String uri, ActorRef ref) {
        eventBusRef.tell(new EventBus.UnsubscribeMessage(uri, ref), ref);
    }

    public void askSubscriptions(ActorRef ref) {
        eventBusRef.tell(new AskSubscriptionsMessage(), ref);
    }


    public void registerPublisher(Class<?> clazz, ActorRef ref) {
        eventBusRef.tell(new EventBus.RegisterPublisherMessage(clazz, ref), ref);
    }

    public void unregisterPublisher(Class<?> clazz, ActorRef ref) {
        eventBusRef.tell(new EventBus.UnregisterPublisherMessage(clazz, ref), ref);
    }

    public static class SubscribeMessage {
        boolean silent;
        Class<?> event;
        ActorRef ref;
        String uri;

        public SubscribeMessage(String uri, ActorRef ref) {
            this.uri = uri;
            this.ref = ref;
        }

        public SubscribeMessage(Class<?> event, ActorRef ref, boolean silent) {
            this.event = event;
            this.ref = ref;
            this.silent = silent;
        }
    }

    public static class UnsubscribeMessage {
        Class<?> event;
        ActorRef ref;
        String uri;

        public UnsubscribeMessage(ActorRef ref) {
            this.ref = ref;
        }

        public UnsubscribeMessage(String uri, ActorRef ref) {
            this.ref = ref;
            this.uri = uri;
        }

        public UnsubscribeMessage(Class<?> event, ActorRef ref) {
            this.event = event;
            this.ref = ref;
        }
    }

    public static class SubscribedMessage {
        Class<?> event;
        public SubscribedMessage(Class<?> event) {
            this.event = event;
        }

        public Class<?> getClazz() {
            return event;
        }
    }

    public static class UnsubscribedMessage {
        Class<?> event;
        public UnsubscribedMessage(Class<?> event) {
            this.event = event;
        }

        public Class<?> getClazz() {
            return event;
        }
    }

    public static class PublishMessage {
        Object object;
        ActorRef sender;
        boolean isSticky;

        public PublishMessage(Object object, ActorRef sender) {
            this.object = object;
            this.sender = sender;
        }

        public PublishMessage(Object object, ActorRef sender, boolean isSticky) {
            this.object = object;
            this.sender = sender;
            this.isSticky = isSticky;
        }
    }

    public static class RegisterPublisherMessage {
        Class<?> event;
        ActorRef ref;

        public RegisterPublisherMessage(Class<?> event, ActorRef ref) {
            this.event = event;
            this.ref = ref;
        }
    }

    public static class UnregisterPublisherMessage {
        Class<?> event;
        ActorRef ref;

        public UnregisterPublisherMessage(Class<?> event, ActorRef ref) {
            this.event = event;
            this.ref = ref;
        }
    }

    public static class AskSubscriptionsMessage {

    }

    public static class SubscriptionsMessage {
        HashSet<Class<?>> subs;

        public SubscriptionsMessage(HashSet<Class<?>> subs) {
            this.subs = subs;
        }

        public HashSet<Class<?>> getSubs() {
            return subs;
        }
    }

    public static class ActivateMessage {
        Class<?> event;
        public ActivateMessage(Class<?> event) {
            this.event = event;
        }
    }

    public static class DeactivateMessage {
        Class<?> event;
        public DeactivateMessage(Class<?> event) {
            this.event = event;
        }
    }
}
