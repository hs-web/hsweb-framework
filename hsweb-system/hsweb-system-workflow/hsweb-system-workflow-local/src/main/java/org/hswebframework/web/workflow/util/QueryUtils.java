package org.hswebframework.web.workflow.util;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.query.Query;
import org.hswebframework.ezorm.core.param.Sort;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Slf4j
public class QueryUtils {

    public static <U, T extends Query<?, U>> PagerResult<U> doQuery(T query, QueryParamEntity entity) {
        return doQuery(query,
                entity,
                Function.identity());
    }

    public static <U, R, T extends Query<?, U>> PagerResult<R> doQuery(T query, QueryParamEntity entity, Function<U, R> mapping) {
        return doQuery(query,
                entity,
                mapping,
                (term, tuQuery) -> log.warn("不支持的查询条件:{} {}", term.getTermType(), term.getColumn()));
    }

    public static <U, R, T extends Query<?, U>> PagerResult<R> doQuery(T query,
                                                                       QueryParamEntity entity,
                                                                       Function<U, R> mapping,
                                                                       BiConsumer<Term, T> notFound) {
        applyQueryParam(query, entity, notFound);
        int total = (int) query.count();
        if (total == 0) {
            return PagerResult.empty();
        }
        entity.rePaging(total);
        List<R> list = query.listPage(entity.getPageIndex(), entity.getPageSize() * (entity.getPageIndex() + 1))
                .stream()
                .map(mapping)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return PagerResult.of(total, list);
    }

    public static <U, T extends Query<?, U>> void applyQueryParam(T query, QueryParamEntity entity, BiConsumer<Term, T> notFound) {
        Class type = query.getClass();
        for (Term term : entity.getTerms()) {
            String name = term.getColumn();
            if (TermType.like.equals(term.getTermType())) {
                name = name.concat("Like");
            } else if (TermType.in.equals(term.getTermType())) {
                name = name.concat("s");
            }
            String finalName = name;
            AtomicBoolean found = new AtomicBoolean(false);
            ReflectionUtils.doWithMethods(type, method -> {
                if (method.getParameterCount() == 1 && (method.getName().equals(finalName) || method.getName().equals(term.getColumn()))) {
                    Object value = FastBeanCopier.DEFAULT_CONVERT.convert(term.getValue(), method.getParameterTypes()[0], FastBeanCopier.EMPTY_CLASS_ARRAY);
                    ReflectionUtils.invokeMethod(method, query, value);
                    found.set(true);
                }
            });
            if (!found.get()) {
                notFound.accept(term, query);
            }
        }
        for (Sort sort : entity.getSorts()) {
            String name = sort.getName();
            Method method = ReflectionUtils.findMethod(type, "orderBy" + StringUtils.toUpperCaseFirstOne(name));
            if (method != null && method.getParameterCount() == 0) {
                ReflectionUtils.invokeMethod(method, query);
                if ("asc".equals(sort.getOrder())) {
                    query.asc();
                } else {
                    query.desc();
                }
            }

        }
    }
}
