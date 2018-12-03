package org.hswebframework.web.commons.entity.param;

import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.QueryEntity;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 查询参数实体,使用<a href="https://github.com/hs-web/hsweb-easy-orm">easyorm</a>进行动态查询参数构建<br>
 * 可通过静态方法创建:<br>
 * {@link QueryParamEntity#empty()}<br>
 * {@link QueryParamEntity#single(String, Object)}<br>
 * 如:
 * <code>
 * QueryParamBean.single("id",id);
 * </code>
 *
 * @author zhouhao
 * @see QueryParam
 * @see Entity
 * @since 3.0
 */
public class QueryParamEntity extends QueryParam implements QueryEntity {

    private static final long serialVersionUID = 8097500947924037523L;

    /**
     * 创建一个空的查询参数实体,该实体无任何参数.
     *
     * @return 无条件的参数实体
     */
    public static QueryParamEntity empty() {
        return new QueryParamEntity();
    }

    /**
     * 创建一个含有单个条件的参数实体,条件默认为is
     *
     * @param field 参数名称
     * @param value 参数值
     * @return 单个条件的参数实体
     * @see QueryParam#where(String, Object)
     */
    public static QueryParamEntity single(String field, Object value) {
        return of(field, value);
    }

    /**
     * @see this#single(String, Object)
     */
    public static QueryParamEntity of(String field, Object value) {
        return empty().where(field, value);
    }

    /**
     * @since 3.0.4
     */
    public static <T> Query<T, QueryParamEntity> newQuery() {
        return Query.empty(new QueryParamEntity());
    }

    /**
     * @since 3.0.4
     */
    public <T> Query<T, QueryParamEntity> toQuery() {
        return Query.empty(this);
    }

    /**
     * 将已有的条件包装到一个嵌套的条件里,并返回一个Query对象.例如:
     * <pre>
     *     entity.toNestQuery().and("userId",userId);
     * </pre>
     * <p>
     * 原有条件: name=? or type=?
     * <p>
     * 执行后条件: (name=? or type=?) and userId=?
     *
     * @see this#toNestQuery(Consumer)
     * @since 3.0.4
     */
    public <T> Query<T, QueryParamEntity> toNestQuery() {
        return toNestQuery(null);
    }

    /**
     * 将已有的条件包装到一个嵌套的条件里,并返回一个Query对象.例如:
     * <pre>
     *     entity.toNestQuery(query->query.and("userId",userId));
     * </pre>
     * <p>
     * 原有条件: name=? or type=?
     * <p>
     * 执行后条件: userId=? (name=? or type=?)
     *
     * @param before 在包装之前执行,将条件包装到已有条件之前
     * @since 3.0.4
     */
    public <T> Query<T, QueryParamEntity> toNestQuery(Consumer<Query<T, QueryParamEntity>> before) {
        List<Term> terms = getTerms();
        setTerms(new ArrayList<>());
        Query<T, QueryParamEntity> query = toQuery();
        if (null != before) {
            before.accept(query);
        }
        return query
                .nest()
                .each(terms, NestConditional::accept)
                .end();
    }


    @Override
    public String toString() {
        return toHttpQueryParamString();
    }

    public QueryParamEntity noPaging() {
        setPaging(false);
        return this;
    }

}
