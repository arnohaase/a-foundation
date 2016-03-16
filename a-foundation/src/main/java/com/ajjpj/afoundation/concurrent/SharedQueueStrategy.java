package com.ajjpj.afoundation.concurrent;


public enum SharedQueueStrategy {
    SyncPush,
    SyncPushWithPrefetch,
    LockPush,
    NonBlockingPush
}
