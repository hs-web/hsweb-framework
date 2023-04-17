package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;
import org.hswebframework.ezorm.core.TermTypeConditionalSupport;
import org.hswebframework.ezorm.core.param.TermType;

public interface JoinOnSpec<Self extends TermTypeConditionalSupport> {

    /**
     * 设置 join on = 条件
     * <pre>{@code
     *   // join detail d on d.id = t.id
     *    is(DetailEntity::getId,MyEntity::getId)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self is(StaticMethodReferenceColumn<T> joinColumn, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.eq, mainOrJoinColumn);
    }

    /**
     * 设置 join on = 条件
     * <pre>{@code
     *   // join detail d on d.id = d2.id
     *    is("id","d2",MyEntity::getId)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param alias            另外一个join表的别名
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self is(StaticMethodReferenceColumn<T> joinColumn,
                            String alias,
                            StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.eq, alias, mainOrJoinColumn);
    }

    /**
     * 设置 join on != 条件
     * <pre>{@code
     *   // join detail d on d.id != t.id
     *    not(DetailEntity::getId,MyEntity::getId)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self not(StaticMethodReferenceColumn<T> joinColumn, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.not, mainOrJoinColumn);
    }

    /**
     * 设置 join on != 条件
     * <pre>{@code
     *   // join detail d on d.id != d2.id
     *    not("id","d2",MyEntity::getId)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param alias            另外一个join表的别名
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self not(StaticMethodReferenceColumn<T> joinColumn,
                            String alias,
                            StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.not, alias, mainOrJoinColumn);
    }

    /**
     * 设置 join on > 条件
     * <pre>{@code
     *   // join detail d on d.max_age > t.age
     *    gt(DetailEntity::getMaxAge,MyEntity::getAge)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self gt(StaticMethodReferenceColumn<T> joinColumn, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.gt, mainOrJoinColumn);
    }

    /**
     * 设置 join on > 条件
     * <pre>{@code
     *   // join detail d on d.max_age > t2.age
     *    gt(DetailEntity::getMaxAge,"t2",MyEntity::getAge)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param alias            另外一个join表的别名
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self gt(StaticMethodReferenceColumn<T> joinColumn, String alias, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.gt, alias, mainOrJoinColumn);
    }


    /**
     * 设置 join on >= 条件
     * <pre>{@code
     *   // join detail d on d.max_age >= t.age
     *    gte(DetailEntity::getMaxAge,MyEntity::getAge)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self gte(StaticMethodReferenceColumn<T> joinColumn, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.gte, mainOrJoinColumn);
    }

    /**
     * 设置 join on >= 条件
     * <pre>{@code
     *   // join detail d on d.max_age >= t2.age
     *    gte(DetailEntity::getMaxAge,"t2",MyEntity::getAge)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param alias            另外一个join表的别名
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self gte(StaticMethodReferenceColumn<T> joinColumn, String alias, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.gte, alias, mainOrJoinColumn);
    }


    /**
     * 设置 join on < 条件
     * <pre>{@code
     *   // join detail d on d.max_age < t.age
     *    lt(DetailEntity::getMaxAge,MyEntity::getAge)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self lt(StaticMethodReferenceColumn<T> joinColumn, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.lt, mainOrJoinColumn);
    }

    /**
     * 设置 join on < 条件
     * <pre>{@code
     *   // join detail d on d.max_age < t2.age
     *    lt(DetailEntity::getMaxAge,"t2",MyEntity::getAge)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param alias            另外一个join表的别名
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self lt(StaticMethodReferenceColumn<T> joinColumn, String alias, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.lt, alias, mainOrJoinColumn);
    }


    /**
     * 设置 join on <= 条件
     * <pre>{@code
     *   // join detail d on d.max_age <= t.age
     *    lte(DetailEntity::getMaxAge,MyEntity::getAge)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self lte(StaticMethodReferenceColumn<T> joinColumn, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.lte, mainOrJoinColumn);
    }

    /**
     * 设置 join on <= 条件
     * <pre>{@code
     *   // join detail d on d.max_age <= t2.age
     *    lte(DetailEntity::getMaxAge,"t2",MyEntity::getAge)
     * }</pre>
     *
     * @param joinColumn       关联表列
     * @param mainOrJoinColumn 主表或者其他关联表列
     * @param alias            另外一个join表的别名
     * @param <T>              T
     * @param <T2>             T2
     * @return this
     */
    default <T, T2> Self lte(StaticMethodReferenceColumn<T> joinColumn, String alias, StaticMethodReferenceColumn<T2> mainOrJoinColumn) {
        return applyColumn(joinColumn, TermType.lte, alias, mainOrJoinColumn);
    }

    /**
     * 设置 join on 字段关联条件
     * <pre>{@code
     *   // join on t.age > d.max_age
     *    applyColumn(MyEntity::getAge,"gt",Detail::getMaxAge)
     * }</pre>
     *
     * @param joinColumn       列名,可以为其他关联表的列名
     * @param termType         条件类型 {@link TermType} {@link org.hswebframework.ezorm.rdb.operator.builder.fragments.TermFragmentBuilder#getId() }
     * @param mainOrJoinColumn 关联表列名
     * @return this
     */
    <T, T2> Self applyColumn(StaticMethodReferenceColumn<T> joinColumn,
                             String termType,
                             StaticMethodReferenceColumn<T2> mainOrJoinColumn);

    /**
     * 设置 join on 字段关联条件
     * <pre>{@code
     *   // join detail d on d.age > d2.max_age
     *    applyColumn(Detail::getAge,"gt","d2",Detail::getMaxAge)
     * }</pre>
     *
     * @param joinColumn       列名,可以为其他关联表的列名
     * @param termType         条件类型 {@link TermType} {@link org.hswebframework.ezorm.rdb.operator.builder.fragments.TermFragmentBuilder#getId() }
     * @param alias            另外一个join表别名
     * @param mainOrJoinColumn 关联表列名
     * @return this
     */
    <T, T2> Self applyColumn(StaticMethodReferenceColumn<T> joinColumn,
                             String termType,
                             String alias,
                             StaticMethodReferenceColumn<T2> mainOrJoinColumn);


}
