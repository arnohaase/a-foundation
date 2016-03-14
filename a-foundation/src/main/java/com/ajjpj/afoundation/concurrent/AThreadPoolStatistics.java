package com.ajjpj.afoundation.concurrent;

import java.util.Arrays;


/**
 * @author arno
 */
public class AThreadPoolStatistics {
    public static final AThreadPoolStatistics NONE = new AThreadPoolStatistics (new AWorkerThreadStatistics[0], new ASharedQueueStatistics[0]);

    public final AWorkerThreadStatistics[] workerThreadStatistics;
    public final ASharedQueueStatistics[] sharedQueueStatisticses;

    public AThreadPoolStatistics (AWorkerThreadStatistics[] workerThreadStatistics, ASharedQueueStatistics[] sharedQueueStatisticses) {
        this.workerThreadStatistics = workerThreadStatistics;
        this.sharedQueueStatisticses = sharedQueueStatisticses;
    }

    @Override public String toString () {
        return "AThreadPoolStatistics{" +
                "workerThreadStatistics=" + Arrays.toString (workerThreadStatistics) +
                ", sharedQueueStatisticses=" + Arrays.toString (sharedQueueStatisticses) +
                '}';
    }
}
