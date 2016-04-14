package com.gabriele.actor.internals;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainThreadDispatcher extends AbstractDispatcher {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private static final MainThreadDispatcher instance = new MainThreadDispatcher();

    public static MainThreadDispatcher getInstance() {
        return instance;
    }

    private MainThreadDispatcher() {
    }

    private ExecutorService service = new ExecutorService() {
        @Override
        public void shutdown() {

        }

        @NonNull
        @Override
        public List<Runnable> shutdownNow() {
            return null;
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
            return false;
        }

        @NonNull
        @Override
        public <T> Future<T> submit(Callable<T> callable) {
            return null;
        }

        @NonNull
        @Override
        public <T> Future<T> submit(Runnable runnable, T t) {
            return null;
        }

        @NonNull
        @Override
        public Future<?> submit(Runnable runnable) {
            return null;
        }

        @NonNull
        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
            return null;
        }

        @NonNull
        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException {
            return null;
        }

        @NonNull
        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public void execute(Runnable runnable) {
            mHandler.post(runnable);
        }
    };

    @Override
    protected ExecutorService getExecutorService() {
        return service;
    }
}
