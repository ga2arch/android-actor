package com.gabriele.actor.internals;

import com.gabriele.actor.utils.Completable;

public class AskActor extends AbstractActor {
    private final Completable completable;

    public AskActor(Completable completable) {
        this.completable = completable;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (!(o instanceof ActorMessage.ControlMessage)) {
            completable.put(o);
            stopSelf();
        }
    }
}
