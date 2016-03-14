package com.ajjpj.afoundation.concurrent;

import java.util.concurrent.TimeoutException;


public class TimeoutExceptionWithoutStackTrace extends TimeoutException {

    @Override public Throwable fillInStackTrace () {
        return this;
    }
}
