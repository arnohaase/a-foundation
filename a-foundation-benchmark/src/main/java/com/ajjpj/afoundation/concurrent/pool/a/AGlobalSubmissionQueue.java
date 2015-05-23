package com.ajjpj.afoundation.concurrent.pool.a;

/**
 * @author arno
 */
public interface AGlobalSubmissionQueue {
    void submit (Runnable task);
}
