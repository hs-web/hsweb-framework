package org.hswebframework.web.id;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class SnowflakeIdGeneratorTest {


    @Test
    public void test(){

        AtomicLong time = new AtomicLong(System.currentTimeMillis());


        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0,1){
            @Override
            protected long timeGen() {
                return time.get();
            }
        };

        System.out.println(generator.nextId());
        //回退1秒
        time.addAndGet(-1000);
        System.out.println(generator.nextId());

        time.addAndGet(2000);
        System.out.println(generator.nextId());

    }

}