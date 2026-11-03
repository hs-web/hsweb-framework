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
    public void shouldBindByIndexWithConvertedPlaceholders() {
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
        assertEquals(Arrays.asList("bind", 0, LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())), calls.get(0));
        assertEquals(Arrays.asList("bindNull", 1, LocalDateTime.class), calls.get(1));
        assertEquals(Arrays.asList("bindNull", 2, String.class), calls.get(2));
        assertEquals(Arrays.asList("bind", "$4", "custom"), calls.get(3));
        assertEquals(4, calls.size());
    }
}
