package com.ajjpj.afoundation.concurrent.jdk;

import com.ajjpj.afoundation.util.AUnchecker;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;


/**
 * This class bridges package visibility for ThreadLocalRandom methods
 *
 * @author arno
 */
public class ThreadLocalRandomHelper {

    public static int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = UNSAFE.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;   // xorshift
            r ^= r >>> 17;
            r ^= r << 5;
        }
        else {
            localInit();
            if ((r = (int)UNSAFE.getLong(t, SEED)) == 0)
                r = 1; // avoid zero
        }
        UNSAFE.putInt(t, SECONDARY, r);
        return r;
    }
    public static void localInit () {
        try {
            LOCAL_INIT.invoke (null);
        }
        catch (Exception e) {
            AUnchecker.throwUnchecked (e);
        }
    }

    public static int getProbe () {
        return UNSAFE.getInt (Thread.currentThread (), PROBE);
    }

    public static int advanceProbe (int probe) {
        probe ^= probe << 13;   // xorshift
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
    }

    private static final Unsafe UNSAFE;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;
    private static final Method LOCAL_INIT;

    static {
        try {
            LOCAL_INIT = ThreadLocalRandom.class.getDeclaredMethod ("localInit");
            LOCAL_INIT.setAccessible (true);

            final Field f = Unsafe.class.getDeclaredField ("theUnsafe");
            f.setAccessible (true);
            UNSAFE = (Unsafe) f.get (null);

            Class<?> tk = Thread.class;
            SEED = UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomSeed"));
            PROBE = UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomProbe"));
            SECONDARY = UNSAFE.objectFieldOffset
                    (tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
