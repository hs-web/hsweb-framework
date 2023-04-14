package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.Conditional;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <pre>{@code
 *
 * // select a.id as `a.id` ,b.name as b.name from table_a a
 * // left join table_b b on a.id=b.id
 * // where b.name like 'zhang%'
 *
 * class R{
 *
 *
 * }
 *
 *   Flux<R> =  helper
 *      .select(R.class)
 *      .as(A::getName,R::setName)
 *      .as(A::getId,R::setAid)
 *      .from(A.class)
 *      .leftJoin(B.class,spec-> spec.alias('b').is(A::id, B::id))
 *      .where(dsl->dsl.like(B::getName,'zhang%'))
 *      .fetch()
 *
 * }</pre>
 */
public interface QueryHelper {

    /**
     * 创建一个查询构造器
     *
     * @param resultType 实体类型
     * @param <R>        类型
     * @return 查询构造器
     */
    <R> SelectColumnMapperSpec<R> select(Class<R> resultType);

    /**
     * 创建一个查询构造器,并返回指定的实体类型
     *
     * @param resultType 实体类型
     * @param mapperSpec 实体映射配置
     * @param <R>        类型
     * @return 查询构造器
     */
    <R> SelectSpec<R> select(Class<R> resultType,
                             Consumer<ColumnMapperSpec<R,?>> mapperSpec);


    interface SelectSpec<R> {

        /**
         * 指定从哪个表查询
         *
         * @param clazz  实体类型
         * @param <From> 实体类型
         * @return 查询构造器
         * @see javax.persistence.Table
         */
        <From> FromSpec<R> from(Class<From> clazz);

    }

    interface SortSpec<R> extends ExecuteSpec<R> {

        SortSpec<R> sort(String column, String order);

        <T> SortSpec<R> sort(Getter<T, ?> column, String order);

    }

    interface WhereSpec<R> extends ExecuteSpec<R> {
        SortSpec<R> where(QueryParamEntity param);

        SortSpec<R> where(Consumer<Conditional<?>> dsl);
    }

    interface FromSpec<R> extends JoinSpec<R>, SortSpec<R> {


    }

    interface JoinSpec<R> extends WhereSpec<R> {

        <T> JoinSpec<R> fullJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on);

        <T> JoinSpec<R> leftJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on);

        <T> JoinSpec<R> innerJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on);

        <T> JoinSpec<R> rightJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on);

    }

    interface ExecuteSpec<R> {

        Flux<R> fetch();

        Mono<PagerResult<R>> fetchPaged();

        Mono<PagerResult<R>> fetchPaged(int pageIndex, int pageSize);
    }

    interface SelectColumnMapperSpec<R> extends ColumnMapperSpec<R,SelectColumnMapperSpec<R>>, SelectSpec<R> {

    }

    interface ColumnMapperSpec<R,Self extends ColumnMapperSpec<R,Self>> {

        Self all(Class<?> tableType);

        <V> Self all(Class<?> tableType, Setter<R, V> setter);

        Self all(String table);

        <V> Self all(String table, Setter<R, V> setter);

        <S, V> Self as(Getter<S, V> getter, Setter<R, V> setter);

        <S, V> Self as(Getter<S, V> getter, String alias);

        <S, V> Self as(String column, Setter<R, V> setter);

        <S, V> Self as(String column, String alias);
    }


    interface Getter<S, V> extends Function<S, V>, Serializable {

    }

    interface Setter<S, V> extends BiConsumer<S, V>, Serializable {

    }
}
