package com.ajjpj.afoundation.concurrent;

import com.ajjpj.afoundation.function.AFunction0NoThrow;

import java.util.concurrent.atomic.AtomicInteger;


class DefaultThreadNameFactory implements AFunction0NoThrow<String> {
    private static final AtomicInteger poolCounter = new AtomicInteger ();

    private final String namePrefix;

    private int poolNumber = -1; // initialize the pool number lazily to avoid burning numbers in pools with other thread name factories
    private int nextThreadNumber = 0;

    DefaultThreadNameFactory (String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override public String apply () {
        if (poolNumber < 0) poolNumber = poolCounter.getAndIncrement ();

        return namePrefix + "#" + poolNumber + "-" + nextThreadNumber++;
    }
}
