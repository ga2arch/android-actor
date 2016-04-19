package com.gabriele.actor.dispatchers;

import android.os.Build;

import com.gabriele.actor.internals.AbstractDispatcher;
import com.gabriele.actor.internals.ActorMessage;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;

public class ForkJoinDispatcher extends AbstractDispatcher {
    private final ExecutorService executorService;

    static final private ForkJoinDispatcher instance = new ForkJoinDispatcher();

    public static ForkJoinDispatcher getInstance() {
        return instance;
    }

    private ForkJoinDispatcher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            executorService = new ForkJoinPool();
        } else {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public Queue<ActorMessage> getMailbox() {
        return new LinkedBlockingQueue<>();
    }
}
