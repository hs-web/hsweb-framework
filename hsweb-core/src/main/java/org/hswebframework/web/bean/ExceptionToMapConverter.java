package org.hswebframework.web.bean;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 异常转 Map 的字段提供者，通过 {@link java.util.ServiceLoader} 注册。
 *
 * <p>字段值必须延迟到 Supplier 被调用时读取，使 copier 能在读取前应用 include/ignore。
 * copier 负责空值跳过和值转换；提供者不应提前生成堆栈等诊断信息。
 * 自定义实现可继承 {@link DefaultExceptionToMapConverter}。
 *
 * @see FastBeanCopier
 */
public interface ExceptionToMapConverter {

    void convert(Throwable source, BiConsumer<String, Supplier<?>> properties);
}
