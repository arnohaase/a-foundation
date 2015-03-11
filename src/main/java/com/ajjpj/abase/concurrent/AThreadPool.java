package com.ajjpj.abase.concurrent;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author arno
 */
public interface AThreadPool extends ATaskScheduler {
    void shutdown ();
    List<Runnable> shutdownNow ();
    boolean isShutdown ();
    boolean isTerminated ();
    boolean awaitTermination (long timeout, TimeUnit unit) throws InterruptedException;
}


