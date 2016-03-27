package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AFunction0NoThrow;
import com.ajjpj.afoundation.function.AFunction1NoThrow;
import com.ajjpj.afoundation.function.AStatement1;
import com.ajjpj.afoundation.function.AStatement1NoThrow;


//TODO no-steal implementation: WakeupForLocalTasks, StealOnIdle, NoStealing
//TODO separate implementation optimized for blocking

public class AThreadPoolBuilder {
    private boolean checkShutdownOnSubmission = true;

    private int numThreads = Runtime.getRuntime ().availableProcessors ();
    private int numSharedQueues = Runtime.getRuntime ().availableProcessors ();
    private int localQueueSize = 16384;
    private int sharedQueueSize = 16384;

    private int prefetchBatchSize = 4;
    private int ownLocalFifoInterval = 100_000;
    private int skipLocalWorkInterval = 100_000;
    private int switchScharedQueueInterval = 1_000_000;
    private int numPrefetchLocal = 0;

    private SharedQueueStrategy sharedQueueStrategy = SharedQueueStrategy.SyncPush;

    private boolean isDaemon = false;
    private AFunction0NoThrow<String> threadNameFactory = new DefaultThreadNameFactory ("AThreadPool");
    private AStatement1NoThrow<Throwable> exceptionHandler = Throwable::printStackTrace;

    private AFunction1NoThrow<AThreadPoolImpl,ASharedQueue> sharedQueueFactory = pool -> {
        switch (sharedQueueStrategy) {
            case SyncPush:        return new SharedQueueBlockPushBlockPopImpl    (prefetchBatchSize, pool, sharedQueueSize);
            case LockPush:        return new SharedQueueNonblockPushBlockPopImpl (prefetchBatchSize, pool, sharedQueueSize);
            case NonBlockingPush: return new SharedQueueNonBlockingImpl          (prefetchBatchSize, pool, sharedQueueSize);
        }
        throw new IllegalStateException ("unknown shared queue strategy " + sharedQueueStrategy);
    };

    public AThreadPoolBuilder withCheckShutdownOnSubmission (boolean checkShutdownOnSubmission) {
        this.checkShutdownOnSubmission = checkShutdownOnSubmission;
        return this;
    }

    public AThreadPoolBuilder withNumThreads (int numThreads) {
        this.numThreads = numThreads;
        return this;
    }

    public AThreadPoolBuilder withNumSharedQueues (int numSharedQueues) {
        this.numSharedQueues = numSharedQueues;
        return this;
    }

    public AThreadPoolBuilder withLocalQueueSize (int localQueueSize) {
        this.localQueueSize = localQueueSize;
        return this;
    }

    public AThreadPoolBuilder withSharedQueueSize (int sharedQueueSize) {
        this.sharedQueueSize = sharedQueueSize;
        return this;
    }

    public AThreadPoolBuilder withSharedQueueStrategy (SharedQueueStrategy strategy) {
        this.sharedQueueStrategy = strategy;
        return this;
    }

    public AThreadPoolBuilder withPrefetchBatchSize (int prefetchBatchSize) {
        this.prefetchBatchSize = prefetchBatchSize;
        return this;
    }

    /**
     * Processing the 'top', i.e. LIFO, element of a thread's local queue is typically desirable because caches tend to still be
     *  hot. It can however lead to starvation with 'old' work never getting done in very specific (pretty pathological) load
     *  scenarios where every work item spawns a new work item. To avoid these starvation scenarios, a WorkerThread reads from
     *  the bottom of its local queue once in a while.
     */
    public AThreadPoolBuilder withOwnLocalFifoInterval (int ownLocalFifoInterval) {
        this.ownLocalFifoInterval = ownLocalFifoInterval;
        return this;
    }

    /**
     * It is usually most efficient for worker threads to only read from their own local queues as long as there is work there.
     *  There are however some load scenarios in which this leads to starvation of work in shared, global queues, and therefore this
     *  parameter exists. It determines how often a worker thread skips local work, even if some is available, and looks for work
     *  elsewhere.
     */
    public AThreadPoolBuilder withSkipLocalWorkInterval (int skipLocalWorkInterval) {
        this.skipLocalWorkInterval = skipLocalWorkInterval;
        return this;
    }

    /**
     *
     */
    public AThreadPoolBuilder switchSwitchSharedQueueInterval (int switchScharedQueueInterval) {
        this.switchScharedQueueInterval = switchScharedQueueInterval;
        return this;
    }

    public AThreadPoolBuilder withNumPrefetchLocal (int numPrefetchLocal) {
        this.numPrefetchLocal = numPrefetchLocal;
        return this;
    }

    /**
     * Completely replaces shared factory creation by custom code. NB: While providing maximum control, calling this method requires a deep understanding
     *  of the interaction between a shared queue and its thread pool. If you are not sure what that means, you should probably not be using this method.
     */
    public AThreadPoolBuilder withSharedQueueFactory (AFunction1NoThrow<AThreadPoolImpl, ASharedQueue> sharedQueueFactory) {
        this.sharedQueueFactory = sharedQueueFactory;
        return this;
    }

    public AThreadPoolBuilder withDaemonThreads (boolean daemonThreads) {
        this.isDaemon = daemonThreads;
        return this;
    }

    public AThreadPoolBuilder withThreadNamePrefix (String threadNamePrefix) {
        this.threadNameFactory = new DefaultThreadNameFactory (threadNamePrefix);
        return this;
    }

    public AThreadPoolBuilder withThreadNameFactory (AFunction0NoThrow<String> threadNameFactory) {
        this.threadNameFactory = threadNameFactory;
        return this;
    }

    public AThreadPoolBuilder withExceptionHandler (AStatement1NoThrow<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public <T extends Throwable> AThreadPoolBuilder log (AStatement1<String, T> logOperation) throws T {
        final String stringRepresentation = toString ();
        logOperation.apply (stringRepresentation);
        return this;
    }

    public AThreadPoolWithAdmin build() {
        return new AThreadPoolImpl (isDaemon, threadNameFactory, exceptionHandler, numThreads, localQueueSize, numSharedQueues, checkShutdownOnSubmission, sharedQueueFactory, ownLocalFifoInterval, numPrefetchLocal, skipLocalWorkInterval, switchScharedQueueInterval);
    }

    @Override
    public String toString () {
        return "AThreadPoolBuilder{" +
                "checkShutdownOnSubmission=" + checkShutdownOnSubmission +
                ", numThreads=" + numThreads +
                ", numSharedQueues=" + numSharedQueues +
                ", localQueueSize=" + localQueueSize +
                ", sharedQueueSize=" + sharedQueueSize +
                ", prefetchBatchSize=" + prefetchBatchSize +
                ", ownLocalFifoInterval=" + ownLocalFifoInterval +
                ", skipLocalWorkInterval=" + skipLocalWorkInterval +
                ", numPrefetchLocal=" + numPrefetchLocal +
                ", sharedQueueStrategy=" + sharedQueueStrategy +
                ", isDaemon=" + isDaemon +
                ", threadNameFactory=" + threadNameFactory +
                ", exceptionHandler=" + exceptionHandler +
                ", sharedQueueFactory=" + sharedQueueFactory +
                '}';
    }
}
