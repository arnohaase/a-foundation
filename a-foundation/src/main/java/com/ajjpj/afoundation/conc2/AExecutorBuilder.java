package com.ajjpj.afoundation.conc2;

/**
 * @author arno
 */
public class AExecutorBuilder {
    private int globalBeforeLocalInterval = 100;
    private int numPollsBeforePark = 1;
    private int pollNanosBeforePark = 100;

    //TODO thread naming
    //TODO error handling strategy
    //TODO local / global queue size (fixed)
    //TODO without work stealing

    public AExecutorBuilder setGlobalBeforeLocalInterval (int globalBeforeLocalInterval) {
        this.globalBeforeLocalInterval = globalBeforeLocalInterval;
        return this;
    }
    public AExecutorBuilder setNumPollsBeforePark (int numPollsBeforePark) {
        this.numPollsBeforePark = numPollsBeforePark;
        return this;
    }
    public AExecutorBuilder setPollNanosBeforePark (int pollNanosBeforePark) {
        this.pollNanosBeforePark = pollNanosBeforePark;
        return this;
    }

    public APromisingExecutor buildPromisingExecutor (int numThreads) {
        return new AWorkStealingPoolImpl (numThreads, globalBeforeLocalInterval, numPollsBeforePark, pollNanosBeforePark).start ();
    }

    public AExecutor buildExecutor (int numThreads) {
        return buildPromisingExecutor (numThreads);
    }

    //TODO build sync executor
}
