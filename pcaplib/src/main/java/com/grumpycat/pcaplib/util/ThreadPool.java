package com.grumpycat.pcaplib.util;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private final ScheduledExecutorService executor;
    private static class InnerClass {
        static ThreadPool instance = new ThreadPool();
    }
    private ThreadPool() {
        executor = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("PCap ThreadPool");
            return thread;
        });

    }
    public static void execute(Runnable runnable){
        InnerClass.instance.executor.execute(runnable);
    }

    public static void schedule(Runnable runnable, long delay){
        InnerClass.instance.executor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }
}
