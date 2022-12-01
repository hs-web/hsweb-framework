package org.hswebframework.web.id;

import io.netty.util.concurrent.FastThreadLocal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomIdGenerator implements IDGenerator<String> {

    // java -Dgenerator.random.instance-id=8
    static final RandomIdGenerator GLOBAL = new RandomIdGenerator(
            Integer.getInteger("generator.random.instance-id", ThreadLocalRandom.current().nextInt()).byteValue()
    );

    private final static FastThreadLocal<byte[]> HOLDER = new FastThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[24];
        }
    };

    private final byte instanceId;

    public static RandomIdGenerator create(byte instanceId) {
        return new RandomIdGenerator(instanceId);
    }

    public static String random() {
        return GLOBAL.generate();
    }

    public String generate() {
        long now = System.currentTimeMillis();
        byte[] value = HOLDER.get();
        value[0] = instanceId;

        value[1] = (byte) (now >>> 32);
        value[2] = (byte) (now >>> 24);
        value[3] = (byte) (now >>> 16);
        value[4] = (byte) (now >>> 8);
        value[5] = (byte) (now);

        nextBytes(value, 6, 8);
        nextBytes(value, 9, 15);
        nextBytes(value, 16, 24);

        return Base64.getUrlEncoder().encodeToString(value);
    }


    private static void nextBytes(byte[] bytes, int offset, int len) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = offset; i < len; ) {
            for (int rnd = random.nextInt(),
                 n = Math.min(len - i, Integer.SIZE / Byte.SIZE);
                 n-- > 0; rnd >>= Byte.SIZE) {
                bytes[i++] = (byte) rnd;
            }
        }

    }


    public static void main(String[] args) {
        System.out.println(random());
        System.out.println(random().length());
        Set<String> distinct = new HashSet<>(100_0000);

        long time = System.currentTimeMillis();
        for (int i = 0; i < 100_0000; i++) {
            distinct.add(random());
        }
        System.out.println(distinct.size());
        System.out.println(System.currentTimeMillis() - time);
    }
}
