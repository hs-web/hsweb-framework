package org.hswebframework.web.crud.sql;

import io.r2dbc.spi.Statement;
import org.hswebframework.ezorm.rdb.executor.NullValue;
import org.hswebframework.ezorm.rdb.executor.R2dbcParameterBinder;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultR2dbcExecutorBindingTest {

    @Test
    public void shouldBindByNameWithConvertedPlaceholders() {
        checkBinding(true, "select $1, $2, $3, $4");
    }

    @Test
    public void shouldBindByIndexWithNativePlaceholders() {
        checkBinding(false, "select ?, ?, ?, ?");
    }

    private void checkBinding(boolean convert, String expectedSql) {
        DefaultR2dbcExecutor executor = new DefaultR2dbcExecutor();
        executor.setBindCustomSymbol(convert);
        List<List<Object>> calls = new ArrayList<>();
        Statement statement = (Statement) Proxy.newProxyInstance(
            Statement.class.getClassLoader(), new Class[]{Statement.class}, (proxy, method, args) -> {
                calls.add(Arrays.asList(method.getName(), args[0], args[1]));
                return proxy;
            });
        Date date = new Date(1712345678901L);
        R2dbcParameterBinder custom = (target, name) -> target.bind(name, "custom");
        SqlRequest request = executor.convertRequest(SqlRequests.prepare(
            "select ?, ?, ?, ?", date, NullValue.of(Date.class, null), null, custom));
        assertEquals(expectedSql, request.getSql());
        assertSame(statement, executor.prepareStatement(statement, request));
        assertEquals(Arrays.asList("bind", convert ? "$1" : 0, LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())), calls.get(0));
        assertEquals(Arrays.asList("bindNull", convert ? "$2" : 1, LocalDateTime.class), calls.get(1));
        assertEquals(Arrays.asList("bindNull", convert ? "$3" : 2, String.class), calls.get(2));
        assertEquals(Arrays.asList("bind", "$4", "custom"), calls.get(3));
        assertEquals(4, calls.size());
    }

    @Test
    public void shouldPreserveReorderedAndRepeatedNativeNames() {
        checkNativeNames("select @arg2 as a, @arg1 as b", 1);
        checkNativeNames("select @arg1 as a, @arg1 as b, @arg2 as c", 1);
    }

    @Test
    public void shouldPreserveZeroBasedNameNumbering() {
        checkNativeNames("select @arg1 as a, @arg0 as b, @arg0 as c", 0);
    }

    private void checkNativeNames(String sql, int firstIndex) {
        R2dbcParameterBinder custom = (target, name) -> target.bind(name, "custom");
        for (Object[] parameters : Arrays.asList(
            new Object[]{11, 22},
            new Object[]{null, 22},
            new Object[]{11, null},
            new Object[]{NullValue.of(Integer.class, null), 22},
            new Object[]{11, NullValue.of(Long.class, null)},
            new Object[]{custom, 22},
            new Object[]{11, custom},
            new Object[]{custom, NullValue.of(Integer.class, null)})) {
            DefaultR2dbcExecutor executor = new DefaultR2dbcExecutor() {
                @Override
                protected int getBindFirstIndex() {
                    return firstIndex;
                }
            };
            executor.setBindCustomSymbol(true);
            executor.setBindSymbol("@arg");
            List<Object> identifiers = new ArrayList<>();
            Statement statement = (Statement) Proxy.newProxyInstance(
                Statement.class.getClassLoader(), new Class[]{Statement.class}, (proxy, method, args) -> {
                    identifiers.add(args[0]);
                    return proxy;
                });
            SqlRequest request = executor.convertRequest(SqlRequests.prepare(sql, parameters));
            assertEquals(sql, request.getSql());
            assertSame(statement, executor.prepareStatement(statement, request));
            assertEquals(Arrays.asList("@arg" + firstIndex, "@arg" + (firstIndex + 1)), identifiers);
        }
    }
}
