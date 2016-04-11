package com.gabriele.actor.internals;

import android.os.Build;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class ForkJoinDispatcher extends AbstractDispatcher {
    private final ExecutorService executorService;

    public ForkJoinDispatcher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            executorService = new ForkJoinPool();
        } else {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
