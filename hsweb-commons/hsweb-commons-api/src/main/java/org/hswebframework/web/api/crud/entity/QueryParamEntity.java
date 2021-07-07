package org.hswebframework.web.api.crud.entity;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.Set;
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
    @Schema(description = "where条件表达式,与terms参数不能共存.语法: name = 张三 and age > 16")
    private String where;

    @Getter
    @Schema(description = "orderBy条件表达式,与sorts参数不能共存.语法: age asc,createTime desc")
    private String orderBy;

    //总数,设置了此值时,在分页查询的时候将不执行count.
    @Getter
    @Setter
    @Schema(description = "设置了此值后将不重复执行count查询总数")
    private Integer total;

    @Getter
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
    public Set<String> getIncludes() {
        return super.getIncludes();
    }

    @Override
    @Schema(description = "指定不查询的列")
    public Set<String> getExcludes() {
        return super.getExcludes();
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
     * 表达式方式排序
     *
     * @param orderBy 表达式
     * @since 4.0.1
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        if (StringUtils.isEmpty(orderBy)) {
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
        if (StringUtils.isEmpty(where)) {
            return;
        }
        setTerms(TermExpressionParser.parse(where));
    }

    @Override
    public List<Term> getTerms() {
        List<Term> terms = super.getTerms();
        if (CollectionUtils.isEmpty(terms) && StringUtils.hasText(where)) {
            setTerms(terms = TermExpressionParser.parse(where));
        }
        return terms;
    }

    public QueryParamEntity noPaging() {
        setPaging(false);
        return this;
    }

    public QueryParamEntity doNotSort(){
        this.setSorts(new ArrayList<>());
        return this;
    }

    @Override
    public QueryParamEntity clone() {
        return (QueryParamEntity) super.clone();
    }
}
