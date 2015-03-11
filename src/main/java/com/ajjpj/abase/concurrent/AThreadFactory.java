package com.ajjpj.abase.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author arno
 */
public class AThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger (1);
    private final AtomicInteger threadNumber = new AtomicInteger (1);
    private final String namePrefix;
    private final boolean isDaemon;

    public static AThreadFactory createWithRunningPoolNumber (String namePrefix, boolean isDaemon) {
        return new AThreadFactory (namePrefix + "-" + poolNumber.getAndIncrement () + "-", isDaemon);
    }

    public static AThreadFactory create (String namePrefix, boolean isDaemon) {
        return new AThreadFactory (namePrefix + "-", isDaemon);
    }

    private AThreadFactory (String namePrefix, boolean isDaemon) {
        this.namePrefix = namePrefix;
        this.isDaemon = isDaemon;
    }

    @Override public Thread newThread (Runnable r) {
        Thread t = new Thread (r, namePrefix + threadNumber.getAndIncrement());
        if (t.isDaemon () != isDaemon) {
            t.setDaemon (isDaemon);
        }
        return t;
    }
}
