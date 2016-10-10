/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.service.impl.lock;

import org.hsweb.concureent.cache.RedisCacheManagerAutoConfig;
import org.hsweb.concurrent.lock.LockFactory;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class LockTest extends AbstractTestCase {

    @Resource
    private LockFactory lockFactory;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testCache() {
        redisTemplate.execute((RedisCallback<? extends Object>) connection -> {
            for (int i = 0; i < 50000; i++) {
                User user = new User();
                user.setName("test" + i);
                try {
                    JdkSerializationRedisSerializer s = new JdkSerializationRedisSerializer();
                    connection.lPush("test".getBytes(), s.serialize(user));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
        long l = System.currentTimeMillis();
        System.out.println(redisTemplate.opsForList().range("test", 0, -1).size());
        System.out.println(System.currentTimeMillis() - l);
    }

    @Test
    public void testLock() throws InterruptedException {
        Lock lock = lockFactory.createLock("test_lock");
        new Thread(() -> {
            try {
                System.out.println("锁");
                lock.lock();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            System.out.println("解锁");
            lock.unlock();
        }).start();
        Thread.sleep(200);
        System.out.println("锁2");
        lock.lock();
        System.out.println("解锁2");
        lock.unlock();
        Thread.sleep(10000);
    }

    @Test
    public void testRWLock() throws InterruptedException {
        ReadWriteLock readWriteLock = lockFactory.createReadWriteLock("test");
        new Thread(() -> {
            try {
                System.out.println("读锁");
                readWriteLock.readLock().lock();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            System.out.println("解读锁");
            readWriteLock.readLock().unlock();
        }).start();
        Thread.sleep(100);
        System.out.println("写锁");
        readWriteLock.writeLock().tryLock(20, TimeUnit.SECONDS);
        Thread.sleep(100);
        System.out.println("解写锁");
        readWriteLock.writeLock().unlock();
        Thread.sleep(10000);
    }
}
