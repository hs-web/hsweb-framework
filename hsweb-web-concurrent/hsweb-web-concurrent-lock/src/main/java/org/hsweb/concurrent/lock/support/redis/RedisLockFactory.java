package org.hsweb.concurrent.lock.support.redis;

import org.hsweb.concurrent.lock.support.DefaultLockFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by zhouhao on 16-5-27.
 */
public class RedisLockFactory extends DefaultLockFactory {
	private RedisTemplate redisTemplate;
	public static ConcurrentSkipListMap<Long, List<String>> READ_LOCKS_BASE = new ConcurrentSkipListMap();

	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 能用，但是这里写的比较烂，并没有提高并发能力
	 * <p>
	 * 唯一的好处，方便计算时间，移除过期未能释放的锁
	 */
	@PostConstruct
	private void autoRemoveReadLock() {
		new Thread(() -> {
			while (true) {
				sleep();
				ConcurrentNavigableMap<Long, List<String>> tempMap = READ_LOCKS_BASE.headMap(new Date().getTime() - RedisReadWriteLock.DEFAULT_EXPIRE * 1000);
				if (tempMap != null && tempMap.size() > 0) {
					for (Map.Entry<Long, List<String>> entry : tempMap.entrySet()) {
						if (entry.getValue() != null && entry.getValue().size() > 0) {
							for (String str : entry.getValue()) {
								for (ReadWriteLock readWriteLock : READ_WRITE_LOCK_BASE.values()) {
									RedisReadWriteLock.ReadLock readLock = (RedisReadWriteLock.ReadLock) readWriteLock.readLock();
									readLock.unlock(str);
								}
//									移除超期未解锁的key
								System.out.println("好像移除了一个哦:" + str);
							}
						}
						READ_LOCKS_BASE.remove(entry.getKey());
					}
				}
			}
		}).start();
	}

	/**
	 * 这里的休眠时间不应该打死
	 * <p>
	 * 理论上，根据当前请求密集度 + 近期超期读锁数   动态调整超期时间才合理
	 */
	private void sleep() {
		try {
			Thread.sleep(RedisReadWriteLock.DEFAULT_EXPIRE);
		} catch (InterruptedException e) {

		}
	}

	@Override
	public ReadWriteLock createReadWriteLock(String key) {
		synchronized (READ_WRITE_LOCK_BASE) {
			ReadWriteLock readWriteLock = READ_WRITE_LOCK_BASE.get(key);
			if (readWriteLock == null) {
				readWriteLock = new RedisReadWriteLock(key, redisTemplate);
				READ_WRITE_LOCK_BASE.put(key, readWriteLock);
			}
			return readWriteLock;
		}
	}

	@Override
	public Lock createLock(String key) {
		synchronized (LOCK_BASE) {
			Lock lock = LOCK_BASE.get(key);
			if (lock == null) {
				lock = new RedisLock(key, redisTemplate);
				LOCK_BASE.put(key, lock);
			}
			return lock;
		}
	}
}
