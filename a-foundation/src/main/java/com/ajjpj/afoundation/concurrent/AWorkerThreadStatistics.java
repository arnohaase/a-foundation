package com.ajjpj.afoundation.concurrent;


/**
 * @author arno
 */
public class AWorkerThreadStatistics {
    public final Thread.State state;

    public final long numTasksExecuted;
    public final long numSharedTasksExecuted;
    public final long numSteals;
    public final long numExceptions;

    public final long numParks;
    public final long numFalseAlarmUnparks;
    public final long numSharedQueueSwitches;

    public final long numLocalSubmits;

    public final int approximateLocalQueueSize;

    public AWorkerThreadStatistics (Thread.State state, long numTasksExecuted, long numSharedTasksExecuted, long numSteals, long numExceptions, long numParks, long numFalseAlarmUnparks,
                                    long numSharedQueueSwitches, long numLocalSubmits, int approximateLocalQueueSize) {
        this.state = state;
        this.numTasksExecuted = numTasksExecuted;
        this.numSharedTasksExecuted = numSharedTasksExecuted;
        this.numSteals = numSteals;
        this.numExceptions = numExceptions;
        this.numParks = numParks;
        this.numFalseAlarmUnparks = numFalseAlarmUnparks;
        this.numSharedQueueSwitches = numSharedQueueSwitches;
        this.numLocalSubmits = numLocalSubmits;
        this.approximateLocalQueueSize = approximateLocalQueueSize;
    }

    @Override
    public String toString () {
        return "AWorkerThreadStatistics{" +
                "state=" + state +
                ", numTasksExecuted=" + numTasksExecuted +
                ", numSharedTasksExecuted=" + numSharedTasksExecuted +
                ", numSteals=" + numSteals +
                ", numExceptions=" + numExceptions +
                ", numParks=" + numParks +
                ", numFalseAlarmUnparks=" + numFalseAlarmUnparks +
                ", numSharedQueueSwitches=" + numSharedQueueSwitches +
                ", numLocalSubmits=" + numLocalSubmits +
                ", approximateLocalQueueSize=" + approximateLocalQueueSize +
                '}';
    }
}
