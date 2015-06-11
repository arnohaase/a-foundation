package com.ajjpj.afoundation.util;


//TODO documentation
/**
 * This class is inspired by Scala's NonFatal object. It centralizes logic for deciding which Throwables are safe to
 *  handle locally - e.g. by logging - and which are not.
 *
 * @author arno
 */
public class AExceptionFilter { //TODO make specific Throwables contributable?
    public static boolean requiresNonLocalHandling (Throwable th) {
        return th instanceof VirtualMachineError ||
                th instanceof ThreadDeath ||
                th instanceof InterruptedException ||
                th instanceof LinkageError;
    }

    /**
     * This method checks if a given Throwable is fit for local handling, returning it if it is and
     *  throwing it otherwise.
     */
    public static <T extends Throwable> T forLocalHandling (T th) {
        if (requiresNonLocalHandling (th)) {
            AUnchecker.throwUnchecked (th);
        }
        return th;
    }
}
