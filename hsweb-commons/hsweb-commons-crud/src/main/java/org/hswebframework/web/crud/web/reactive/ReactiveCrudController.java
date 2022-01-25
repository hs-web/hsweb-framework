package org.hswebframework.web.crud.web.reactive;

/**
 * 通用响应式增删该查Controller,实现本接口来默认支持增删改查相关操作.
 *
 * @param <E> 实体类型
 * @param <K> 主键类型
 * @see ReactiveSaveController
 * @see ReactiveQueryController
 * @see ReactiveDeleteController
 */
public interface ReactiveCrudController<E, K> extends
        ReactiveSaveController<E, K>,
        ReactiveQueryController<E, K>,
        ReactiveDeleteController<E, K> {
}
