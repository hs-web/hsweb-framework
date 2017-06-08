package org.hswebframework.web.concurrent.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 读锁注解,在方法上注解,则对此方法加锁.
 *
 * @author zhouhao
 * @see org.hswebframework.web.concurrent.lock.LockManager
 * @see ReadWriteLock#readLock()
 * @since 3.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ReadLock {
    /**
     * 锁名,支持表达式,表达式使用 ${} 进行标识;如果此值为空,则使用方法名称作为锁名
     * e.g.
     * <pre>
     *     &#064;ReadLock("my_lock_${#id}")
     *     public void foo(String id){
     *
     *     }
     *
     *     &#064;ReadLock(value="my_lock_${#id}",condition="#id!=null")
     *     public void foo(String id){
     *
     *     }
     * </pre>
     *
     * @return 锁名称, 支持spel表达式
     * @see org.hswebframework.web.concurrent.lock.LockManager#getReadWriteLock(String)
     */
    String[] value() default {};

    /**
     * 锁的条件表达式,当满足条件的时候才执行锁
     * e.g.
     * <pre>
     *     &#064;ReadLock(value="my_lock_${#id}",condition="#id!=null")
     *     public void foo(String id){
     *
     *     }
     * </pre>
     * @return 条件表达式
     */
    String condition() default "";

    /**
     * 超时时间,超过此时间不能获取锁则抛出异常{@link InterruptedException},如果设置为-1,则认为不设置超时时间
     *
     * @return 超时时间, 默认10秒
     */
    long timeout() default 10;

    /**
     * @return 超时时间单位, 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
