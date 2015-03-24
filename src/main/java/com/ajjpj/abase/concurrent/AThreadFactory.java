package com.ajjpj.abase.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class is a customizable implementation of the <code>ThreadFactory</code> interface from <code>java.util.concurrent</code>. It can specify whether threads
 *  should be created as daemon or not, and it can specify a naming scheme.
 *
 * @author arno
 */
public class AThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger (1);
    private final AtomicInteger threadNumber = new AtomicInteger (1);
    private final String namePrefix;
    private final boolean isDaemon;

    /**
     * Creates a <code>ThreadFactory</code> with a naming scheme consisting of a constant prefix followed by two numbers, the first uniquely identifying this thread factory
     *  instance and the second uniquely identifying the thread created within the tread factory. <p>
     * Thread names are globally unique. If in doubt, use this factory method.
     *
     * @param namePrefix the prefix used for all thread names, followed by running numbers
     * @param isDaemon the flag specifying whether threads are created as daemon or not. Daemon threads are 'background' threads in the sense that they do not prevent JVM termination.
     */
    public static AThreadFactory createWithRunningPoolNumber (String namePrefix, boolean isDaemon) {
        return new AThreadFactory (namePrefix + "-" + poolNumber.getAndIncrement () + "-", isDaemon);
    }

    /**
     * Creates a <code>ThreadFactory</code> with a naming scheme consisting of a constant prefix followed by one number. The number is a counter inside the newly created
     *  <code>ThreadFactory> instance, so names are unique per instance but not globally.<p>
     * Use this factory method if the prefix is used only for one thread factory.
     *
     * @param namePrefix the prefix used for all thread names, followed by a running number
     * @param isDaemon the flag specifying whether threads are created as daemon or not. Daemon threads are 'background' threads in the sense that they do not prevent JVM termination.
     */
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
