package org.hswebframework.web.id;

import io.netty.util.concurrent.FastThreadLocal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomIdGenerator implements IDGenerator<String> {

    // java -Dgenerator.random.instance-id=8
    static final RandomIdGenerator GLOBAL = new RandomIdGenerator(
            Integer.getInteger("generator.random.instance-id", ThreadLocalRandom.current().nextInt(1, 127)).byteValue()
    );

    static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

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
        nextBytes(value, 8, 16);
        nextBytes(value, 16, 24);
        return encoder.encodeToString(value);
    }

    public static boolean isRandomId(String id) {
        if (id.length() < 16 || id.length() > 48) {
            return false;
        }
        return org.apache.commons.codec.binary.Base64.isBase64(id);
    }

    public static boolean timestampRangeOf(String id, Duration duration) {
        try {
            if (!isRandomId(id)) {
                return false;
            }
            long now = System.currentTimeMillis();
            long ts = getTimestampInId(id);
            return Math.abs(now - ts) <= duration.toMillis();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static long getTimestampInId(String id) {
        byte[] bytes = Base64.getUrlDecoder().decode(id);
        if (bytes.length < 6) {
            return -1;
        }
        long now = System.currentTimeMillis();
        return ((now >>> 56) & 0xff) << 56 |
                ((now >>> 48) & 0xff) << 48 |
                ((now >>> 40) & 0xff) << 40 |
                ((long) bytes[1] & 0xff) << 32 |
                ((long) bytes[2] & 0xff) << 24 |
                ((long) bytes[3] & 0xff) << 16 |
                ((long) bytes[4] & 0xff) << 8 |
                (long) bytes[5] & 0xff;
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

}
