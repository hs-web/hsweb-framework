package org.hswebframework.web.dao.mybatis.builder;

import org.apache.commons.beanutils.BeanMap;
import org.hswebframework.ezorm.core.Conditional;
import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.Param;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.QueryEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author zhouhao
 * @since 3.0
 */
public class SqlParamParser {

    public static QueryParamEntity parseQueryParam(Object param) {
        return new QueryParamParser().parse(param).get();
    }

    private static class QueryParamParser {
        private Query<?, QueryParamEntity> query = Query.empty(new QueryParamEntity());

        private BiConsumer<String, Object> consumer = (k, v) -> {
            if (k.endsWith("$or")) {
                k = k.substring(0, k.length() - 3);
                query.or(k, v);
            } else {
                query.and(k, v);
            }
        };

        private QueryParamParser parse(Object obj) {
            if (obj instanceof Map) {
                ((Map) obj).forEach((k, v) -> {
                    String key = String.valueOf(k);
                    if ("pageIndex".equals(key)) {
                        query.getParam().setPageIndex(StringUtils.toInt(v));
                    }
                    if ("pageSize".equals(key)) {
                        query.getParam().setPageSize(StringUtils.toInt(v));
                    }
                    if (v != null) {
                        if (v instanceof Entity || v instanceof Map) {
                            List<Term> terms = new QueryParamParser().parse(v).get().getTerms();
                            Term term = new Term();
                            term.setType(key.equalsIgnoreCase("or") ? Term.Type.or : Term.Type.and);
                            term.setTerms(terms);
                            query.getParam().getTerms().add(term);
                        } else {
                            consumer.accept(String.valueOf(key), v);
                        }
                    }
                });
            } else {
                Map map = new HashMap<>(new BeanMap(obj));
                map.remove("class");
                map.remove("declaringClass");
                parse(map);
            }
            return this;
        }

        private QueryParamEntity get() {
            return query.getParam();
        }
    }
}
