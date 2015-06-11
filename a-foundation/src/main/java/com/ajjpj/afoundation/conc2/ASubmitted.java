package com.ajjpj.afoundation.conc2;

/**
 * @author arno
 */
public interface ASubmitted {
    void cancel (boolean interrupt);

    boolean isCancelled();
    boolean isFinished();
}
