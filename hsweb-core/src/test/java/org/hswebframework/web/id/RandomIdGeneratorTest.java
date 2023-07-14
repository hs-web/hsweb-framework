package org.hswebframework.web.id;

import org.junit.Test;

import static org.junit.Assert.*;

public class RandomIdGeneratorTest {


    @Test
    public void test() {
        RandomIdGenerator.GLOBAL.generate();

        long now = System.currentTimeMillis();
        String id = RandomIdGenerator.GLOBAL.generate();
        long ts = RandomIdGenerator.getTimestampInId(id);

        System.out.println(now + ">" + ts);
        assertTrue(ts >= now);
    }
}