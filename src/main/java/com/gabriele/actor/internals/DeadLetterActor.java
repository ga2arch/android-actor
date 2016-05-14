package com.gabriele.actor.internals;

import com.gabriele.actor.logging.Logger;

public class DeadLetterActor extends AbstractActor {

    Logger log;

    @Override
    public void preStart() {
        log = Logger.create(getSelf());
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof ActorMessage.DeadLetter) {
            onDeadLetter((ActorMessage.DeadLetter) o);
        }
    }

    private void onDeadLetter(ActorMessage.DeadLetter deadLetter) {
        final ActorRef sender = getSender();
        final ActorRef recipient = deadLetter.recipient;
        final Object message = deadLetter.messsage;

        log.debug(String.format("[%s] didn't not receive message %s from [%s]",
                recipient.getPath(), message, sender.getPath()));
    }
}
