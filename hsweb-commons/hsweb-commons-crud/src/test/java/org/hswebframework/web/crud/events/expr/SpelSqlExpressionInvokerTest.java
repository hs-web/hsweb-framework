package org.hswebframework.web.crud.events.expr;

import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.junit.jupiter.api.Test;
import reactor.function.Function3;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class SpelSqlExpressionInvokerTest {


    @Test
    void test() {
        SpelSqlExpressionInvoker invoker = new SpelSqlExpressionInvoker();

        BiFunction<Object[], Map<String, Object>, Object> func = invoker.compile("name + 1 + ?");

        assertEquals(13, func.apply(new Object[]{2}, Collections.singletonMap("name", 10)));

    }

    @Test
    void testFunction() {
        SpelSqlExpressionInvoker invoker = new SpelSqlExpressionInvoker();

        BiFunction<Object[], Map<String, Object>, Object> func = invoker.compile("coalesce(name,?)");

        assertEquals(2, func.apply(new Object[]{2}, Collections.emptyMap()));

        assertEquals(3, func.apply(null, Collections.singletonMap("name",3)));

    }
}