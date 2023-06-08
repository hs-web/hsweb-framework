package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.Conditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 使用DSL方式链式调用来构建复杂查询
 *
 * <pre>{@code
 *
 * // select a.id as `a.id` ,b.name as b.name from table_a a
 * // left join table_b b on a.id=b.id
 * // where b.name like 'zhang%'
 *
 *   Flux<R> =  helper
 *      .select(R.class)
 *      .as(A::getName,R::setName)
 *      .as(A::getId,R::setAid)
 *      .from(A.class)
 *      .leftJoin(B.class,spec-> spec.is(A::id, B::id))
 *      .where(dsl->dsl.like(B::getName,'zhang%'))
 *      .fetch();
 *
 * }</pre>
 * <p>
 * 使用原生SQL方式来构建动态条件查询
 * <pre>{@code
 *      helper
 *       .select("select * from table_a a left join table_b b on a.id=b.id",R::new)
 *       .where(dsl->dsl.like(R::getName,'zhang%'))
 *       .fetch();
 *  }</pre>
 *
 * @author zhouhao
 * @since 4.0.16
 */
public interface QueryHelper {

    /**
     * 基于SQL创建分析器
     *
     * @param selectSql SQL
     * @return QueryAnalyzer
     */
    QueryAnalyzer analysis(String selectSql);

    /**
     * 逻辑和{@link QueryHelper#select(String, Object...)}相同,将查询结果转换为指定的实体类
     *
     * @param sql         SQL
     * @param newInstance 实体类实例化方法
     * @param args        参数
     * @param <T>         实体类型
     * @return NativeQuerySpec
     */
    <T> NativeQuerySpec<T> select(String sql,
                                  Supplier<T> newInstance,
                                  Object... args);

    /**
     * 创建原生SQL查询器
     * <p>
     * 预编译参数仅支持<code>?</code>,如果要使用模版,请使用{@link org.hswebframework.ezorm.rdb.executor.SqlRequests#template(String, Object)}
     * 构造sql以及参数
     * <pre>{@code
     *
     *  Flux<Record> records = helper
     *        .select("select * from table where type = ?",type)
     *         //注入动态查询条件
     *        .where(param)
     *        //或者编程式构造动态条件
     *        .where(dsl->dsl.is("name",name))
     *        //执行查询
     *        .fetch();
     * }</pre>
     * <p>
     * join逻辑:
     *
     * <pre>{@code
     *
     *  helper.select("select t1.id,t2.* from table t1"+
     *                " left join table2 t2 on t1.id = t2.id") ...
     *
     *  将返回结构:
     *   [
     *     {
     *     "id":"t1.id的值",
     *     "t2.c1":"t2的字段"
     *     }
     *   ]
     * }</pre>
     *
     * <p>
     * ⚠️注意：避免动态拼接SQL语句,应该使用预编译参数或者动态注入动态条件来进行条件处理.
     *
     * @param sql  SQL查询语句
     * @param args 预编译参数
     * @return 查询构造器
     */
    NativeQuerySpec<Record> select(String sql, Object... args);


    /**
     * 创建一个查询构造器
     *
     * @param resultType 实体类型,必须明确定义实体类,不能使用{@link java.util.Map}等类型
     * @param <R>        类型
     * @return 查询构造器
     */
    <R> SelectColumnMapperSpec<R> select(Class<R> resultType);

    /**
     * 创建一个查询构造器,并返回指定的实体类型
     *
     * @param resultType 实体类型,必须明确定义实体类,不能使用{@link java.util.Map}等类型
     * @param mapperSpec 实体映射配置
     * @param <R>        类型
     * @return 查询构造器
     */
    <R> SelectSpec<R> select(Class<R> resultType,
                             Consumer<ColumnMapperSpec<R, ?>> mapperSpec);


    interface NativeQuerySpec<T> extends ExecuteSpec<T> {

        /**
         * 设置日志,在执行sql等操作时使用次日志进行日志打印.
         *
         * @param logger Logger
         * @return this
         */
        NativeQuerySpec<T> logger(Logger logger);

        /**
         * 以DSL方式构造查询条件
         * <pre>{@code
         *  helper
         *  .select("select * from table t")
         *  .where(dsl->dsl.is("type","device"))
         * }</pre>
         *
         * @param dsl DSL
         * @return this
         */
        default ExecuteSpec<T> where(Consumer<Query<?, QueryParamEntity>> dsl) {
            Query<?, QueryParamEntity> query = QueryParamEntity.newQuery();
            dsl.accept(query);
            return where(query.getParam());
        }

        /**
         * 指定动态查询条件,通常用于前端动态传入查询条件
         * <pre>{@code
         *  helper
         *  .select("select * from table t")
         *  .where(param)
         *  .fetch()
         * }</pre>
         *
         * @param param DSL
         * @return this
         */
        ExecuteSpec<T> where(QueryParamEntity param);

    }

    interface SelectSpec<R> {

        /**
         * 指定从哪个表查询
         *
         * @param clazz  实体类型,类上需要注解{@link javax.persistence.Table},并使用{@link javax.persistence.Column}来描述列
         * @param <From> 实体类型
         * @return 查询构造器
         * @see javax.persistence.Table
         */
        <From> FromSpec<R> from(Class<From> clazz);

    }


    /**
     * 查询条件构造器
     *
     * @param <R> 查询结果类型
     */
    interface WhereSpec<R> extends ExecuteSpec<R> {

        /**
         * 使用动态查询参数来作为查询条件,用于通过参数传递查询条件的场景
         *
         * @param param 查询参数
         * @return 排序描述
         * @see QueryParamEntity
         */
        SortSpec<R> where(QueryParamEntity param);

        /**
         * 使用DSL方式来构造查询条件,用于编程式的构造查询条件
         * <pre>{@code
         *
         *   // where t.name = ? or age > 18
         *   where(dsl->dsl.is(MyEntity::getName,name).or().gt(MyEntity::getAge,18))
         *
         * }</pre>
         *
         * @param dsl DSL条件构造接收器
         * @return 排序描述
         */
        SortSpec<R> where(Consumer<Conditional<?>> dsl);
    }


    /**
     * 排序构造器
     *
     * @param <R> 查询结果类型
     */
    interface SortSpec<R> extends ExecuteSpec<R> {

        /**
         * 使用指定的列名进行正序排序,多次执行将使用多列排序
         * <pre>{@code
         *  // order by a.index asc
         *  orderByAsc("a.index");
         * }</pre>
         *
         * @param column 列名
         * @return 排序构造器
         */
        default SortSpec<R> orderByAsc(String column) {
            return orderBy(column, SortOrder.Order.asc);
        }

        /**
         * 使用指定的列名进行倒序排序,多次执行将使用多列排序
         * <pre>{@code
         *  // order by a.index desc
         *  orderByDesc("a.index");
         * }</pre>
         *
         * @param column 列名
         * @return 排序构造器
         */
        default SortSpec<R> orderByDesc(String column) {
            return orderBy(column, SortOrder.Order.desc);
        }

        /**
         * 使用指定的列名进行排序,多次执行将使用多列排序
         * <pre>{@code
         *  // order by a.index asc
         *  orderBy("a.index",SortOrder.Order.asc);
         * }</pre>
         *
         * @param column 列名
         * @param order  排序方式
         * @return 排序构造器
         */
        SortSpec<R> orderBy(String column,
                            SortOrder.Order order);


        /**
         * 对方法应用对应的列名进行正序排序,多次执行将使用多列排序
         * <pre>{@code
         *
         *  // order by sort_order asc
         *  orderByAsc(MyEntity::getSortOrder)
         *
         * }</pre>
         *
         * @param column 方法引用
         * @param <S>    S
         * @return 排序构造器
         */
        default <S> SortSpec<R> orderByAsc(Getter<S, ?> column) {
            return orderBy(column, SortOrder.Order.asc);
        }

        /**
         * 对方法应用对应的列名进行倒序排序,多次执行将使用多列排序
         * <pre>{@code
         *
         *  // order by sort_order desc
         *  orderByDesc(MyEntity::getSortOrder)
         *
         * }</pre>
         *
         * @param column 方法引用
         * @param <S>    S
         * @return 排序构造器
         */
        default <S> SortSpec<R> orderByDesc(Getter<S, ?> column) {
            return orderBy(column, SortOrder.Order.desc);
        }

        /**
         * 对方法应用对应的列名进行排序,多次执行将使用多列排序
         * <pre>{@code
         *
         *  // order by sort_order desc
         *  orderBy(MyEntity::getSortOrder,SortOrder.Order.desc)
         *
         * }</pre>
         *
         * @param column 方法引用
         * @param <S>    S
         * @return 排序构造器
         */
        <S> SortSpec<R> orderBy(Getter<S, ?> column,
                                SortOrder.Order order);


    }

    interface FromSpec<R> extends JoinSpec<R>, SortSpec<R> {


    }

    /**
     * 表关联构造器
     *
     * @param <R> 查询结果类型
     */
    interface JoinSpec<R> extends WhereSpec<R> {


        /**
         * 对指定的实体类进行 left join
         *
         * <pre>{@code
         *   // left join detail on my.id = detail.id
         *   leftJoin(DetailEntity.class,spec->spec.is(MyEntity::getId,DetailEntity::getId)
         * }</pre>
         *
         * @param type 实体类型,需要注解{@link javax.persistence.Table}
         * @param on   关联条件构造器
         * @param <T>  T
         * @return 表关联构造器
         */
        <T> JoinSpec<R> leftJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on);

        /**
         * 对指定的实体类进行 right join
         *
         * <pre>{@code
         *   // left join detail on my.id = detail.id
         *   rightJoin(DetailEntity.class,spec->spec.is(MyEntity::getId,DetailEntity::getId)
         * }</pre>
         *
         * @param type 实体类型,需要注解{@link javax.persistence.Table}
         * @param on   关联条件构造器
         * @param <T>  T
         * @return 表关联构造器
         */
        <T> JoinSpec<R> rightJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on);

        /**
         * 对指定的实体类进行 inner join
         *
         * <pre>{@code
         *   // inner join detail on my.id = detail.id
         *   innerJoin(DetailEntity.class,spec->spec.is(MyEntity::getId,DetailEntity::getId)
         * }</pre>
         *
         * @param type 实体类型,需要注解{@link javax.persistence.Table}
         * @param on   关联条件构造器
         * @param <T>  T
         * @return 表关联构造器
         */
        <T> JoinSpec<R> innerJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on);

        /**
         * 对指定的实体类进行 full join
         *
         * <pre>{@code
         *   // join t1 on t1.id = t2.id
         *   fullJoin(DetailEntity.class,spec->spec.is(MyEntity::getId,DetailEntity::getId)
         * }</pre>
         *
         * @param type 实体类型,需要注解{@link javax.persistence.Table}
         * @param on   关联条件构造器
         * @param <T>  T
         * @return 表关联构造器
         */
        <T> JoinSpec<R> fullJoin(Class<T> type, Consumer<JoinConditionalSpec<?>> on);


    }

    /**
     * 执行查询
     *
     * @param <R>
     */
    interface ExecuteSpec<R> {

        /**
         * 执行count查询
         *
         * @return count
         */
        Mono<Integer> count();

        /**
         * 执行查询,返回数据流
         *
         * @return 数据流
         */
        Flux<R> fetch();

        /**
         * 执行分页查询,默认返回第一页的25条数据.
         *
         * @return 分页结果
         */
        Mono<PagerResult<R>> fetchPaged();

        default <T> Mono<PagerResult<T>> fetchPaged(Function<List<R>, Mono<List<T>>> transfer) {
            return transformPageResult(fetchPaged(), transfer);
        }

        /**
         * 指定分页执行查询
         *
         * @param pageIndex 分页序号,从0开始
         * @param pageSize  每页数量
         * @return 分页结果
         */
        Mono<PagerResult<R>> fetchPaged(int pageIndex, int pageSize);

        default <T> Mono<PagerResult<T>> fetchPaged(int pageIndex, int pageSize, Function<List<R>, Mono<List<T>>> transfer) {
            return transformPageResult(fetchPaged(pageIndex, pageSize), transfer);
        }
    }

    interface SelectColumnMapperSpec<R> extends ColumnMapperSpec<R, SelectColumnMapperSpec<R>>, SelectSpec<R> {

    }

    /**
     * 列名映射构造器
     *
     * @param <R>    查询结果类型
     * @param <Self> Self
     */
    interface ColumnMapperSpec<R, Self extends ColumnMapperSpec<R, Self>> {

        /**
         * 查询指定类型对应的表的全部字段.
         *
         * @param tableType 类型,只能是from或者join的类型.
         * @return Self
         */
        Self all(Class<?> tableType);

        /**
         * 查询指定类型对应的表的全部字段并映射到结果类型的一个字段中.
         *
         * <pre>{@code
         *   all(DetailEntity.class,MyEntity::setDetail)
         * }</pre>
         *
         * @param tableType 类型,只能是from或者join的类型.
         * @return Self
         */
        <V> Self all(Class<?> tableType, Setter<R, V> setter);

        /**
         * 查询指定表的全部字段.
         *
         * @param tableOrAlias 表名或者join别名,只能是from或者join的表.
         * @return Self
         */
        Self all(String tableOrAlias);

        /**
         * 查询指定类型对应的表的全部字段并映射到结果类型的一个字段中.
         *
         * <pre>{@code
         *   all("detail",MyEntity::setDetail)
         * }</pre>
         *
         * @param tableOrAlias 表名或者join别名,只能是from或者join的表.
         * @return Self
         */
        <V> Self all(String tableOrAlias, Setter<R, V> setter);

        /**
         * 指定查询的列名,以及映射到结果类型的字段.
         * <pre>{@code
         *   as(DetailEntity::getName,MyEntity::setDetailName)
         * }</pre>
         *
         * @param column 列名
         * @param target 结果类型字段
         * @param <S>    S
         * @param <V>    V
         * @return Self
         */
        <S, V> Self as(Getter<S, V> column, Setter<R, V> target);

        /**
         * 指定查询的列名,以及映射到结果类型的字段.
         * <pre>{@code
         *   as(DetailEntity::getName,"detail.name")
         * }</pre>
         *
         * @param column 列名
         * @param target 结果类型字段
         * @param <S>    S
         * @param <V>    V
         * @return Self
         */
        <S, V> Self as(Getter<S, V> column, String target);

        /**
         * 指定查询的列名,以及映射到结果类型的字段.
         *
         * <pre>{@code
         *   as("_d.name",MyEntity::setDetailName)
         * }</pre>
         *
         * @param column 列名
         * @param target 结果类型字段
         * @return Self
         */
        <V> Self as(String column, Setter<R, V> target);

        /**
         * 指定查询的列名,以及映射到结果类型的字段.
         * <pre>{@code
         *   as("_d.name","detail.name")
         * }</pre>
         *
         * @param column 列名
         * @param target 结果类型字段
         * @return Self
         */
        Self as(String column, String target);
    }

    /**
     * Getter接口定义,只能使用方法引用实现此接口,如:
     *
     * <pre>{@code
     *   MyEntity::getId
     * }</pre>
     *
     * @param <S>
     * @param <V>
     */
    interface Getter<S, V> extends Function<S, V>, Serializable {

    }

    /**
     * Setter接口定义,只能使用方法引用实现此接口,如:
     *
     * <pre>{@code
     *   MyEntity::setId
     * }</pre>
     *
     * @param <S>
     * @param <V>
     */
    interface Setter<S, V> extends BiConsumer<S, V>, Serializable {

    }


    @SuppressWarnings("all")
    static <S, T> Mono<PagerResult<T>> transformPageResult(Mono<PagerResult<S>> source, Function<List<S>, Mono<List<T>>> transfer) {
        return source.flatMap(result -> {
            if (result.getTotal() > 0) {
                return transfer
                        .apply(result.getData())
                        .map(newDataList -> {
                            PagerResult<T> pagerResult = PagerResult.of(result.getTotal(), newDataList);
                            pagerResult.setPageIndex(result.getPageIndex());
                            pagerResult.setPageSize(result.getPageSize());
                            return pagerResult;
                        });
            }
            //empty
            return Mono.just((PagerResult<T>) result);
        });
    }

}
