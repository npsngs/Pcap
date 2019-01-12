package com.grumpycat.pcaplib.util;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private final ScheduledExecutorService executor;
    private final ScheduledExecutorService saveCacheExecutor;
    private final ScheduledExecutorService uiExecutor;
    private static class InnerClass {
        static ThreadPool instance = new ThreadPool();
    }
    private ThreadPool() {
        executor = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("PCap Thread");
            return thread;
        });

        saveCacheExecutor = Executors.newSingleThreadScheduledExecutor(r->{
            Thread thread = new Thread(r);
            thread.setName("Save Cache Thread");
            return thread;
        });

        uiExecutor = Executors.newSingleThreadScheduledExecutor(r->{
            Thread thread = new Thread(r);
            thread.setName("UI Worker Thread");
            return thread;
        });

    }
    public static void execute(Runnable runnable){
        InnerClass.instance.executor.execute(runnable);
    }

    public static void schedule(Runnable runnable, long delay){
        InnerClass.instance.executor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public static void saveCache(Runnable runnable){
        InnerClass.instance.saveCacheExecutor.submit(runnable);
    }

    public static void runUIWorker(Runnable runnable){
        InnerClass.instance.uiExecutor.execute(runnable);
    }
}
