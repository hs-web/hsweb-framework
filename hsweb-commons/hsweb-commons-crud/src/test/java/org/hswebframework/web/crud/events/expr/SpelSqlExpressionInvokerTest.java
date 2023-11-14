package org.hswebframework.web.crud.events.expr;

import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.function.Function3;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class SpelSqlExpressionInvokerTest {


    @Test
    void test() {
        SpelSqlExpressionInvoker invoker = new SpelSqlExpressionInvoker();

        Function3<EntityColumnMapping, Object[], Map<String, Object>, Object> func = invoker.compile("name + 1 + ?");

        EntityColumnMapping mapping = Mockito.mock(EntityColumnMapping.class);

        assertEquals(13, func.apply(mapping, new Object[]{2}, Collections.singletonMap("name", 10)));

    }

    @Test
    void testFunction() {
        SpelSqlExpressionInvoker invoker = new SpelSqlExpressionInvoker();
        EntityColumnMapping mapping = Mockito.mock(EntityColumnMapping.class);

        Function3<EntityColumnMapping, Object[], Map<String, Object>, Object> func = invoker.compile("coalesce(name,?)");

        assertEquals(2, func.apply(mapping, new Object[]{2}, Collections.emptyMap()));

        assertEquals(3, func.apply(mapping, null, Collections.singletonMap("name", 3)));

    }

    @Test
    void testAddNull(){
        SpelSqlExpressionInvoker invoker = new SpelSqlExpressionInvoker();
        EntityColumnMapping mapping = Mockito.mock(EntityColumnMapping.class);

        Function3<EntityColumnMapping, Object[], Map<String, Object>, Object> func = invoker.compile("IFNULL(test,0) + ?");
        assertEquals(2, func.apply(mapping, new Object[]{2}, Collections.emptyMap()));

    }

    @Test
    void testSnake() {
        SpelSqlExpressionInvoker invoker = new SpelSqlExpressionInvoker();
        EntityColumnMapping mapping = Mockito.mock(EntityColumnMapping.class);

        {
            Function3<EntityColumnMapping,Object[], Map<String, Object>, Object> func = invoker.compile("count_value + ?");

            assertEquals(12, func.apply(mapping,new Object[]{2}, Collections.singletonMap("countValue", 10)));

        }
        {
            Mockito.when(mapping.getPropertyByColumnName("_count_v"))
                   .thenReturn(java.util.Optional.of("countValue"));
            Function3<EntityColumnMapping,Object[], Map<String, Object>, Object> func = invoker.compile("_count_v + ?");

            assertEquals(12, func.apply(mapping,new Object[]{2}, Collections.singletonMap("countValue", 10)));

        }


    }

}