package org.hswebframework.web.bean;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 异常与 Map 的双向转换，通过 {@link java.util.ServiceLoader} 注册。
 *
 * <p>字段值必须延迟到 Supplier 被调用时读取，使 copier 能在读取前应用 include/ignore。
 * copier 负责空值跳过和值转换；提供者不应提前生成堆栈等诊断信息。
 * 自定义实现可继承 {@link DefaultExceptionConverter}。
 *
 * @see FastBeanCopier
 */
public interface ExceptionConverter {

    void toMap(Throwable source, BiConsumer<String, Supplier<?>> properties);

    /**
     * 从异常描述恢复可赋值给目标声明的异常，不按远端 type 任意加载类。
     * 不支持的目标或无效描述应明确抛错，不能返回 null 或不兼容的异常。
     */
    Throwable fromMap(Map<String, ?> source, Class<? extends Throwable> targetType);
}
