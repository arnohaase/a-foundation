package com.ajjpj.afoundation.concurrent;

import java.text.NumberFormat;


/**
 * @author arno
 */
public class ASharedQueueStatistics {
    public final int approximateSize;

    public ASharedQueueStatistics (int approximateSize) {
        this.approximateSize = approximateSize;
    }

    @Override public String toString () {
        return "ASharedQueueStatistics{" +
                "approximateSize=" + NumberFormat.getNumberInstance ().format (approximateSize) +
                '}';
    }
}
