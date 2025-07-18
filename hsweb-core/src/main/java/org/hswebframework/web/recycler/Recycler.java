package org.hswebframework.web.recycler;

import reactor.function.Function3;
import reactor.function.Function4;
import reactor.function.Function5;
import reactor.function.Function6;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 对象回收器接口,用于对象的重用和回收管理.
 *
 * @param <T> 被回收的对象类型
 * @author zhouhao
 * @since 5.0.1
 */
public interface Recycler<T> {


    /**
     * 获取针对StringBuilder的Recycler
     *
     * @return Recycler&gt;StringBuilder&lt;
     */
    static Recycler<StringBuilder> stringBuilder() {
        return Recyclers.STRING_BUILDER;
    }

    /**
     * 创建一个回收器实例,请使用静态变量持有Recycler对象.
     *
     * <pre>{@code
     * static file Recycler<StringBuilder> builderPool= Recycler.create(StringBuilder::new,);
     *
     * }</pre>
     *
     * @param builder 对象构造器
     * @param rest    对象重置器
     * @param size    队列大小
     * @param <T>     对象类型
     * @return 回收器实例
     */
    static <T> Recycler<T> create(Supplier<T> builder, Consumer<T> rest, int size) {
        return new RecyclerImpl<>(size, builder, rest);
    }

    /**
     * 使用回收器执行操作
     *
     * @param call 执行的操作
     * @param <R>  返回值类型
     * @return 操作结果
     */
    default <R> R doWith(Function<T, R> call) {
        return doWith(
            call, null, null, null, null,
            (val, a1, a2, a3, a4, a5) -> a1.apply(val));
    }

    /**
     * 使用回收器执行带参数的操作
     *
     * @param arg0 参数
     * @param call 执行的操作
     * @param <A>  参数类型
     * @param <R>  返回值类型
     * @return 操作结果
     */
    default <A, R> R doWith(A arg0, BiFunction<T, A, R> call) {
        return doWith(
            call, arg0, null, null, null,
            (val, a1, a2, a3, a4, a5) -> a1.apply(val, a2));
    }

    /**
     * 使用回收器执行带参数的操作
     *
     * @param arg0 参数
     * @param call 执行的操作
     * @param <A>  参数类型
     * @param <R>  返回值类型
     * @return 操作结果
     */
    default <A, A1, R> R doWith(A arg0, A1 arg1, Function3<T, A, A1, R> call) {
        return doWith(
            call, arg0, arg1, null, null,
            (val, a1, a2, a3, a4, a5) -> a1.apply(val, a2, a3));
    }


    /**
     * 使用回收器执行带参数的操作
     *
     * @param arg0 参数
     * @param call 执行的操作
     * @param <A>  参数类型
     * @param <R>  返回值类型
     * @return 操作结果
     */
    default <A, A1, A2, R> R doWith(A arg0, A1 arg1, A2 arg2, Function4<T, A, A1, A2, R> call) {
        return doWith(
            call, arg0, arg1, arg2, null,
            (val, a1, a2, a3, a4, a5) -> a1.apply(val, a2, a3, a4));
    }

    /**
     * 使用回收器执行带参数的操作
     *
     * @param arg0 参数
     * @param call 执行的操作
     * @param <A>  参数类型
     * @param <R>  返回值类型
     * @return 操作结果
     */
    default <A, A1, A2, A3, R> R doWith(A arg0, A1 arg1, A2 arg2, A3 arg3, Function5<T, A, A1, A2, A3, R> call) {
        return doWith(
            call, arg0, arg1, arg2, arg3,
            (val, a1, a2, a3, a4, a5) -> a1.apply(val, a2, a3, a4, a5));
    }


    /**
     * 使用回收器执行带参数的操作
     *
     * @param arg0 参数
     * @param call 执行的操作
     * @param <A>  参数类型
     * @param <R>  返回值类型
     * @return 操作结果
     */
    <A, A1, A2, A3, A4, R> R doWith(A arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, Function6<T, A, A1, A2, A3, A4, R> call);

    /**
     * 从回收器中提取一个对象,使用完成后请调用{@link Recyclable#recycle()}.
     *
     * @param synchronous 是否同步,如果不会跨线程使用,可设置为true.
     * @return Recyclable
     */
    Recyclable<T> take(boolean synchronous);


}
