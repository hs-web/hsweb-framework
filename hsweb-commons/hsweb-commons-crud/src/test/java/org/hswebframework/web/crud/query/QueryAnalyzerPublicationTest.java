package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Test;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 暂停首次索引构建，验证并发查询不能读到尚未填充完成的列缓存。 */
public class QueryAnalyzerPublicationTest {

    @Test
    public void selectPublishesOnlyCompleteColumnIndex() throws Exception {
        var column = new QueryAnalyzer.Column("state", "state", "reading", null);
        var columns = new GatedList<>(List.of(column));
        var select = new QueryAnalyzer.Select(columns, new QueryAnalyzer.Table("reading", null));
        assertAtomicLookup(() -> select.findColumn("state"), columns);
    }

    @Test
    public void analyzerPublishesOnlyCompleteColumnMappings() throws Exception {
        var metadata = new RDBDatabaseMetadata(Dialect.H2);
        var schema = new RDBSchemaMetadata("public");
        metadata.addSchema(schema);
        metadata.setCurrentSchema(schema);
        var table = spy(new RDBTableMetadata());
        table.setName("reading");
        schema.addTable(table);
        var column = new RDBColumnMetadata();
        column.setName("state");
        column.setAlias("state");
        table.addColumn(column);
        var database = mock(DatabaseOperator.class);
        when(database.getMetadata()).thenReturn(metadata);
        var analyzer = new QueryAnalyzerImpl(database, "select * from reading");
        var columns = new GatedList<>(List.of(column));
        doReturn(columns).when(table).getColumns();
        assertAtomicLookup(() -> analyzer.findColumn("state"), columns);
    }

    private static void assertAtomicLookup(Supplier<Optional<QueryAnalyzer.Column>> lookup,
                                           GatedList<?> columns) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(lookup::get);
            assertTrue(columns.entered.await(5, TimeUnit.SECONDS));
            var readerStarted = new CountDownLatch(1);
            var second = executor.submit(() -> {
                readerStarted.countDown();
                return lookup.get();
            });
            assertTrue(readerStarted.await(5, TimeUnit.SECONDS));
            try {
                // 构建仍暂停时，另一个读者必须等待，不能将半成品索引解释为字段不存在。
                assertThrows(TimeoutException.class, () -> second.get(200, TimeUnit.MILLISECONDS));
            } finally {
                columns.release.countDown();
            }
            assertEquals("state", first.get(5, TimeUnit.SECONDS).orElseThrow().getName());
            assertEquals("state", second.get(5, TimeUnit.SECONDS).orElseThrow().getName());
        } finally {
            columns.release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private static final class GatedList<T> extends AbstractList<T> {
        private final List<T> values;
        private final AtomicBoolean paused = new AtomicBoolean();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        private GatedList(List<T> values) { this.values = values; }

        @Override
        public Iterator<T> iterator() {
            if (paused.compareAndSet(false, true)) {
                entered.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("index build was not released");
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(error);
                }
            }
            return values.iterator();
        }

        @Override public T get(int index) { return values.get(index); }
        @Override public int size() { return values.size(); }
    }
}
