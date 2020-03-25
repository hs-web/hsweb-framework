package org.hswebframework.web.api.crud.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.springframework.util.StringUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 查询参数实体,使用<a href="https://github.com/hs-web/hsweb-easy-orm">easyorm</a>进行动态查询参数构建<br>
 * 可通过静态方法创建:<br>
 * 如:
 * <code>
 * QueryParamEntity.of("id",id);
 * </code>
 *
 * @author zhouhao
 * @see QueryParam
 * @since 3.0
 */
@Slf4j
public class QueryParamEntity extends QueryParam {

    private static final long serialVersionUID = 8097500947924037523L;

    @Getter
    @Deprecated
    private String termExpression;

    @Getter
    private String where;

    @Getter
    private String orderBy;

    //总数,设置了此值时,在分页查询的时候将不执行count.
    @Getter
    @Setter
    private Integer total;

    /**
     * 创建一个空的查询参数实体,该实体无任何参数.
     *
     * @return 无条件的参数实体
     */
    public static QueryParamEntity of() {
        return new QueryParamEntity();
    }


    /**
     * @see this#of(String, Object)
     */
    public static QueryParamEntity of(String field, Object value) {
        return of().and(field, TermType.eq, value);
    }

    /**
     * @since 3.0.4
     */
    public static <T> Query<T, QueryParamEntity> newQuery() {
        return Query.of(new QueryParamEntity());
    }

    /**
     * @since 3.0.4
     */
    public <T> Query<T, QueryParamEntity> toQuery() {
        return Query.of(this);
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
        if (terms.isEmpty()) {
            return query;
        }
        return query
                .nest()
                .each(terms, NestConditional::accept)
                .end();
    }

    /**
     * 设置条件表达式,可以通过表达式的方式快速构建查询条件. 表达式是类似sql条件的语法,如:
     * <pre>
     *     name is 测试 and age gte 10
     * </pre>
     * <pre>
     *     name is 测试 and (age gt 10 or age lte 90 )
     * </pre>
     *
     * @param termExpression 表达式
     * @since 3.0.5
     */
    @Deprecated
    public void setTermExpression(String termExpression) {
        this.termExpression = termExpression;
        log.warn("termExpression is deprecated,please use where.");
        setWhere(termExpression);
    }

    /**
     * 表达式方式排序
     *
     * @param orderBy 表达式
     * @since 4.0.1
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        setSorts(TermExpressionParser.parseOrder(orderBy));
    }

    /**
     * 表达式查询条件,没有SQL注入问题,放心使用
     *
     * @param where 表达式
     * @since 4.0.1
     */
    public void setWhere(String where) {
        this.where = where;
        setTerms(TermExpressionParser.parse(termExpression));
    }

    @Override
    public List<Term> getTerms() {
        List<Term> terms = super.getTerms();
        if (CollectionUtils.isEmpty(terms) && StringUtils.hasText(termExpression)) {
            setTerms(terms = TermExpressionParser.parse(termExpression));
        }
        return terms;
    }

    public QueryParamEntity noPaging() {
        setPaging(false);
        return this;
    }

}
