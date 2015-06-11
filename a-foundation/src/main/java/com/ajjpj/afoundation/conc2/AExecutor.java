package com.ajjpj.afoundation.conc2;


/**
 * @author arno
 */
public interface AExecutor {
    ASubmitted submit (Runnable code);
    void shutdown (boolean cancelBacklog, boolean interruptRunningTasks);
}
