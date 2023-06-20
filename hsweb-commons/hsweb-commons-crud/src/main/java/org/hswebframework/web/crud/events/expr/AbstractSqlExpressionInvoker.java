package org.hswebframework.web.crud.events.expr;

import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.hswebframework.web.crud.events.SqlExpressionInvoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public abstract class AbstractSqlExpressionInvoker implements SqlExpressionInvoker {

    private final Map<String, BiFunction<Object[], Map<String, Object>, Object>> compiled =
            new ConcurrentHashMap<>();

    @Override
    public Object invoke(NativeSql sql, EntityColumnMapping mapping, Map<String, Object> object) {
        return compiled.computeIfAbsent(sql.getSql(), this::compile)
                       .apply(sql.getParameters(), object);
    }


    protected abstract BiFunction<Object[], Map<String, Object>, Object> compile(String sql);

}
