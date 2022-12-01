package org.hswebframework.web.id;

import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class RandomIdGenerator {

    private final static ThreadLocal<byte[]> HOLDER = ThreadLocal.withInitial(() -> new byte[24]);


    public static String random() {
        return random(HOLDER.get());
    }

    public static String random(byte[] container) {

        ThreadLocalRandom.current().nextBytes(container);

        return Base64.getUrlEncoder().encodeToString(container);
    }

}
