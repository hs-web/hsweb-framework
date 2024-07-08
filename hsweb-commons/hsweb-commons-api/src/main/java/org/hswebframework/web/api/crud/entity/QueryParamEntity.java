package org.hswebframework.web.api.crud.entity;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.Param;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.bean.FastBeanCopier;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 查询参数实体,使用<a href="https://github.com/hs-web/hsweb-easy-orm">easyorm</a>进行动态查询参数构建<br>
 * 可通过静态方法创建:<br>
 * 如:
 * <pre>
 * {@code
 *      QueryParamEntity.of("id",id);
 * }
 * </pre>
 * <p>
 * 或者使用DSL方式来构造:
 * <pre>{@code
 *  QueryParamEntity
 *  .newQuery()
 *  .where("id",1)
 *  .execute(service::query)
 * }</pre>
 *
 * @author zhouhao
 * @see QueryParam
 * @since 3.0
 */
@Getter
@Slf4j
public class QueryParamEntity extends QueryParam {

    private static final long serialVersionUID = 8097500947924037523L;

    @Schema(description = "where条件表达式,与terms参数不能共存.语法: name = 张三 and age > 16")
    private String where;

    @Schema(description = "orderBy条件表达式,与sorts参数不能共存.语法: age asc,createTime desc")
    private String orderBy;

    //总数,设置了此值时,在分页查询的时候将不执行count.
    @Setter
    @Schema(description = "设置了此值后将不重复执行count查询总数")
    private Integer total;

    /**
     * @see TermExpressionParser#parse(Map)
     * @since 4.0.17
     */
    @Getter
    @Schema(description = "使用map方式传递查询条件.与terms参数不能共存.格式: {\"name$like\":\"张三\"}")
    private Map<String, Object> filter;

    @Setter
    @Schema(description = "是否进行并行分页")
    private boolean parallelPager = false;

    @Override
    @Hidden
    public boolean isForUpdate() {
        return super.isForUpdate();
    }

    @Override
    @Hidden
    public int getThinkPageIndex() {
        return super.getThinkPageIndex();
    }

    @Override
    @Hidden
    public int getPageIndexTmp() {
        return super.getPageIndexTmp();
    }

    @Override
    @Schema(description = "指定要查询的列")
    @Nonnull
    public Set<String> getIncludes() {
        return super.getIncludes();
    }

    @Override
    @Schema(description = "指定不查询的列")
    @Nonnull
    public Set<String> getExcludes() {
        return super.getExcludes();
    }

    /**
     * 基于另外一个条件参数来创建查询条件实体
     *
     * @param param 参数
     * @return 新的查询条件
     * @since 4.0.14
     */
    public static QueryParamEntity of(Param param) {
        if (param instanceof QueryParamEntity) {
            return ((QueryParamEntity) param).clone();
        }
        return FastBeanCopier.copy(param, new QueryParamEntity());
    }

    /**
     * 创建一个空的查询参数实体,该实体无任何参数.
     *
     * @return 无条件的参数实体
     */
    public static QueryParamEntity of() {
        return new QueryParamEntity();
    }


    /**
     * @see QueryParamEntity#of(String, Object)
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
     * @see QueryParamEntity#toNestQuery(Consumer)
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
     * 表达式方式排序
     *
     * @param orderBy 表达式
     * @since 4.0.1
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        if (!StringUtils.hasText(orderBy)) {
            return;
        }
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
        if (!StringUtils.hasText(where)) {
            return;
        }
        setTerms(TermExpressionParser.parse(where));
    }

    /**
     * 设置map格式的过滤条件
     *
     * @param filter 过滤条件
     * @see TermExpressionParser#parse(Map)
     * @since 4.0.17
     */
    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
        if (MapUtils.isNotEmpty(filter)) {
            setTerms(TermExpressionParser.parse(filter));
        }
    }

    @Override
    @Nonnull
    public List<Term> getTerms() {
        List<Term> terms = super.getTerms();
        if (CollectionUtils.isEmpty(terms) && StringUtils.hasText(where)) {
            setTerms(terms = TermExpressionParser.parse(where));
        }
        if (CollectionUtils.isEmpty(terms) && MapUtils.isNotEmpty(filter)) {
            setTerms(terms = TermExpressionParser.parse(filter));
        }
        return terms;
    }

    @SuppressWarnings("unchecked")
    public QueryParamEntity noPaging() {
        setPaging(false);
        return this;
    }

    public QueryParamEntity doNotSort() {
        this.setSorts(new ArrayList<>());
        return this;
    }

    @Override
    public QueryParamEntity clone() {
        return (QueryParamEntity) super.clone();
    }
}
