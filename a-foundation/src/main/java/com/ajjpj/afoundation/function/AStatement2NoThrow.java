package com.ajjpj.afoundation.function;


/**
 * @author arno
 */
public interface AStatement2NoThrow<P1, P2> extends AStatement2<P1, P2, RuntimeException> {
    @Override void apply (P1 param1, P2 param2);
}
