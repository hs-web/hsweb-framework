package org.hswebframework.web.id;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

public class RandomIdGeneratorTest {


    @Test
    public void test() {
        RandomIdGenerator.GLOBAL.generate();

        long now = System.currentTimeMillis();
        String id = RandomIdGenerator.GLOBAL.generate();
        System.out.println(id + "-->" + id.length());
        long ts = RandomIdGenerator.getTimestampInId(id);

        System.out.println(now + ">" + ts);
        assertTrue(RandomIdGenerator.isRandomId(id));
        assertTrue(RandomIdGenerator.timestampRangeOf(id, Duration.ofMillis(100)));
        assertTrue(ts >= now);
    }
}