package org.hswebframework.web.id;

import io.netty.util.concurrent.FastThreadLocal;

import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class RandomIdGenerator {

    private final static FastThreadLocal<byte[]> HOLDER = new FastThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[24];
        }
    };


    public static String random() {
        return random(HOLDER.get());
    }

    public static String random(byte[] container) {

        ThreadLocalRandom.current().nextBytes(container);

        return Base64.getUrlEncoder().encodeToString(container);
    }

}
