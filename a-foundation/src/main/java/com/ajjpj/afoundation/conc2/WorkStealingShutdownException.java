package com.ajjpj.afoundation.conc2;

/**
 * This class is used internally to signal that a thread pool is shut down.
 *
 * @author arno
 */
class WorkStealingShutdownException extends RuntimeException {
    @Override public Throwable fillInStackTrace () {
        return this;
    }
}
