package org.hswebframework.web.recycler;

/**
 * 可回收对象接口，封装了从回收器中提取的对象
 *
 * <p>该接口代表一个从 {@link Recycler} 中提取的对象包装器，提供了对象访问和回收的功能。
 * 使用完毕后必须调用 {@link #recycle()} 方法将对象归还给回收器，以便重用。
 *
 * <h3>使用模式：</h3>
 * <pre>{@code
 * // 从回收器中获取可回收对象
 * Recyclable<StringBuilder> recyclable = recycler.take(true);
 * try {
 *     // 使用对象
 *     StringBuilder sb = recyclable.get();
 *     sb.append("Hello World");
 *     String result = sb.toString();
 * } finally {
 *     // 必须回收对象
 *     recyclable.recycle();
 * }
 * }</pre>
 *
 * <h3>资源管理：</h3>
 * <p>使用 try-with-resources 模式可以自动管理资源：
 * <pre>{@code
 * try (Recyclable<StringBuilder> recyclable = recycler.take(true)) {
 *     StringBuilder sb = recyclable.get();
 *     sb.append("Hello World");
 *     return sb.toString();
 * } // 自动调用 recycle()
 * }</pre>
 *
 * <h3>线程安全性：</h3>
 * <p>Recyclable 实例本身不是线程安全的，不应在多个线程间共享。每个线程应该从
 * 回收器中获取自己的 Recyclable 实例。
 *
 * <h3>重要注意事项：</h3>
 * <ul>
 *   <li>使用完毕后必须调用 {@link #recycle()} 方法</li>
 *   <li>不要在 recycle() 后继续使用对象</li>
 *   <li>不要多次调用 recycle() 方法</li>
 *   <li>不要在多个线程间共享 Recyclable 实例</li>
 * </ul>
 *
 * @param <T> 被包装的对象类型
 * @author zhouhao
 * @see Recycler
 * @see Recycler#take(boolean)
 * @since 5.0.1
 */
public interface Recyclable<T> extends AutoCloseable {

    /**
     * 获取被包装的对象实例
     *
     * <p>返回从回收器中提取的对象实例。该对象可能是新创建的，也可能是从对象池中重用的。
     * 在调用 {@link #recycle()} 之前，可以安全地使用返回的对象。
     *
     * <h4>使用注意：</h4>
     * <ul>
     *   <li>返回的对象状态已经通过重置器清理</li>
     *   <li>不要在 recycle() 后继续使用返回的对象</li>
     *   <li>不要缓存返回的对象引用</li>
     * </ul>
     *
     * @return 被包装的对象实例，永远不会为 null
     */
    T get();

    /**
     * 回收对象到回收器中
     *
     * <p>将对象归还给回收器，以便后续重用。调用此方法后，不应再使用该对象。
     * 对象会被重置器清理状态，然后放入对象池中等待下次使用。
     *
     * <h4>回收过程：</h4>
     * <ol>
     *   <li>检查是否需要回收（队列未满）</li>
     *   <li>调用重置器清理对象状态</li>
     *   <li>将对象放入队列或ThreadLocal池</li>
     *   <li>标记当前 Recyclable 为已回收状态</li>
     * </ol>
     *
     * <h4>重要提醒：</h4>
     * <ul>
     *   <li>此方法只能调用一次</li>
     *   <li>调用后不要再访问对象</li>
     *   <li>如果队列已满，对象可能不会被回收</li>
     * </ul>
     *
     * @throws IllegalStateException 如果对象已经被回收
     */
    void recycle();

    /**
     * 实现 AutoCloseable 接口，支持 try-with-resources 语法
     *
     * <p>默认实现直接调用 {@link #recycle()} 方法，使得可以在 try-with-resources
     * 语句中自动回收对象。
     */
    @Override
    default void close() {
        recycle();
    }
}
