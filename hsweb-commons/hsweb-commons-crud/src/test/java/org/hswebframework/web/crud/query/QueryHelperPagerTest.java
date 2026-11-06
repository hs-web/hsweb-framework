package org.hswebframework.web.crud.query;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.exception.ValidationException;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QueryHelperPagerTest {

    @Test
    public void testParallelPagerSharesOneInFlightPreparation() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setParallelPager(true);
        AtomicInteger loaded = new AtomicInteger();
        Sinks.One<Integer> preparation = Sinks.one();
        List<QueryOperationContext> scopes = new ArrayList<>();
        ReactiveQuery<Integer> query = mockPreparedQuery(
            () -> preparation.asMono().doOnSubscribe(ignore -> loaded.incrementAndGet()),
            scopes);

        StepVerifier.create(QueryHelper.queryPager(source, () -> query, PagerQueryPolicy.clamp(10)))
            .then(() -> {
                assertEquals(1, loaded.get());
                assertEquals(2, scopes.size());
                preparation.tryEmitValue(1).orThrow();
            })
            .assertNext(result -> {
                assertEquals(1, result.getTotal());
                assertEquals(List.of(1), result.getData());
            })
            .verifyComplete();

        assertSame(scopes.get(0), scopes.get(1));
    }

    @Test
    public void testSequentialPagerReusesPreparationAndResubscriptionGetsFreshScope() {
        QueryParamEntity source = QueryParamEntity.of();
        AtomicInteger loaded = new AtomicInteger();
        List<QueryOperationContext> scopes = new ArrayList<>();
        ReactiveQuery<Integer> query = mockPreparedQuery(
            () -> Mono.fromSupplier(loaded::incrementAndGet),
            scopes);
        Mono<PagerResult<Integer>> pager = QueryHelper.queryPager(source, () -> query);

        StepVerifier.create(pager)
            .assertNext(result -> {
                assertEquals(1, result.getTotal());
                assertEquals(List.of(1), result.getData());
            })
            .verifyComplete();
        StepVerifier.create(pager)
            .assertNext(result -> {
                assertEquals(2, result.getTotal());
                assertEquals(List.of(2), result.getData());
            })
            .verifyComplete();

        assertEquals(2, loaded.get());
        assertSame(scopes.get(0), scopes.get(1));
        assertSame(scopes.get(2), scopes.get(3));
        assertFalse(scopes.get(0) == scopes.get(2));
    }

    @Test
    public void testWarnPolicyKeepsExplicitLargePageForCompatibility() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setPageSize(20);
        source.setTotal(100);
        List<QueryParamEntity> appliedParams = new ArrayList<>();
        ReactiveQuery<Integer> query = mockQuery(
            Mono.just(100),
            Flux.range(0, 100),
            appliedParams);

        StepVerifier
            .create(QueryHelper.queryPager(
                source,
                () -> query,
                new PagerQueryPolicy(10, PagerQueryPolicy.OverflowPolicy.WARN)))
            .assertNext(result -> assertPage(result, 100, 20, 20))
            .verifyComplete();

        assertEquals(20, appliedParams.get(0).getPageSize());
        assertEquals(20, source.getPageSize());
    }

    @Test
    public void testReactorContextPolicyIsReadAtSubscription() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setPageSize(100);
        source.setTotal(200);
        List<QueryParamEntity> appliedParams = new ArrayList<>();
        ReactiveQuery<Integer> query = mockQuery(
            Mono.just(200),
            Flux.range(0, 100),
            appliedParams);
        PagerQueryPolicy policy = PagerQueryPolicy.clamp(11);

        StepVerifier
            .create(QueryHelper
                .queryPager(source, () -> query)
                .contextWrite(context -> PagerQueryPolicy.writeTo(context, policy)))
            .assertNext(result -> assertPage(result, 200, 11, 11))
            .verifyComplete();

        assertSame(policy, PagerQueryPolicy.from(
            reactor.util.context.Context.of(PagerQueryPolicy.class, policy)));
        assertEquals(11, appliedParams.get(0).getPageSize());
    }

    @Test
    public void testKnownTotalUsesCustomMaxAndDoesNotMutateSource() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setPageSize(100);
        source.setTotal(200);
        List<QueryParamEntity> appliedParams = new ArrayList<>();
        AtomicBoolean cancelled = new AtomicBoolean();
        ReactiveQuery<Integer> query = mockQuery(
            Mono.just(200),
            Flux.range(0, 100).doOnCancel(() -> cancelled.set(true)),
            appliedParams);

        StepVerifier
            .create(QueryHelper.queryPager(source, () -> query, 10))
            .assertNext(result -> assertPage(result, 200, 10, 10))
            .verifyComplete();

        assertEquals(100, source.getPageSize());
        assertEquals(1, appliedParams.size());
        assertEquals(10, appliedParams.get(0).getPageSize());
        assertTrue(appliedParams.get(0).isPaging());
        assertTrue(cancelled.get());
        verify(query, never()).count();
    }

    @Test
    public void testNonPositivePageSizeFallsBackToBoundedDefault() {
        PagerQueryPolicy policy = PagerQueryPolicy.clamp(100);
        int expectedPageSize = Math.min(
            Math.max(QueryParam.DEFAULT_PAGE_SIZE, 1),
            policy.getMaxPageSize());
        QueryParamEntity zero = QueryParamEntity.of();
        zero.setPageSize(0);
        QueryParamEntity negative = QueryParamEntity.of();
        negative.setPageSize(-1);

        assertEquals(expectedPageSize, policy.normalize(zero).getPageSize());
        assertEquals(expectedPageSize, policy.normalize(negative).getPageSize());
        assertEquals(0, zero.getPageSize());
        assertEquals(-1, negative.getPageSize());
        assertEquals(10, PagerQueryPolicy.clamp(10).normalize(zero).getPageSize());
    }

    @Test
    public void testPageSizeWithinLimitIsKept() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setPageSize(50);

        QueryParamEntity normalized = PagerQueryPolicy.clamp(100).normalize(source);

        assertEquals(50, normalized.getPageSize());
        assertEquals(50, source.getPageSize());
        assertSame(PagerQueryPolicy.defaults(), PagerQueryPolicy.from(Context.empty()));
    }

    @Test
    public void testNoPagingIsBoundedEvenWithWarnPolicy() {
        QueryParamEntity source = QueryParamEntity.of().noPaging();
        source.setPageSize(Integer.MAX_VALUE);
        source.setTotal(200);
        List<QueryParamEntity> appliedParams = new ArrayList<>();
        ReactiveQuery<Integer> query = mockQuery(
            Mono.just(200),
            Flux.range(0, 100),
            appliedParams);

        StepVerifier
            .create(QueryHelper.queryPager(
                source,
                () -> query,
                new PagerQueryPolicy(12, PagerQueryPolicy.OverflowPolicy.WARN)))
            .assertNext(result -> assertPage(result, 200, 12, 12))
            .verifyComplete();

        assertFalse(source.isPaging());
        assertTrue(appliedParams.get(0).isPaging());
        assertEquals(12, appliedParams.get(0).getPageSize());
    }

    @Test
    public void testParallelPagerIsBounded() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setParallelPager(true);
        source.setPageSize(100);
        List<QueryParamEntity> appliedParams = new ArrayList<>();
        ReactiveQuery<Integer> query = mockQuery(
            Mono.just(200),
            Flux.range(0, 100),
            appliedParams);

        StepVerifier
            .create(QueryHelper.queryPager(source, () -> query, 15))
            .assertNext(result -> assertPage(result, 200, 15, 15))
            .verifyComplete();

        assertEquals(2, appliedParams.size());
        assertTrue(appliedParams.stream().allMatch(param -> param.getPageSize() == 15));
        verify(query).count();
    }

    @Test
    public void testSequentialPagerIsBoundedAndMapsData() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setPageSize(Integer.MAX_VALUE);
        List<QueryParamEntity> appliedParams = new ArrayList<>();
        ReactiveQuery<Integer> query = mockQuery(
            Mono.just(200),
            Flux.range(0, 100),
            appliedParams);

        StepVerifier
            .create(QueryHelper.queryPager(source, () -> query, Object::toString, 20))
            .assertNext(result -> {
                assertPage(result, 200, 20, 20);
                assertEquals("0", result.getData().get(0));
                assertEquals("19", result.getData().get(19));
            })
            .verifyComplete();

        assertEquals(2, appliedParams.size());
        assertTrue(appliedParams.stream().allMatch(param -> param.getPageSize() == 20));
        assertEquals(Integer.MAX_VALUE, source.getPageSize());
    }

    @Test
    public void testZeroTotalSkipsDataQuery() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setPageSize(10);
        List<QueryParamEntity> appliedParams = new ArrayList<>();
        ReactiveQuery<Integer> query = mockQuery(
            Mono.just(0),
            Flux.error(new AssertionError("fetch should not be called")),
            appliedParams);

        StepVerifier
            .create(QueryHelper.queryPager(source, () -> query, 100))
            .assertNext(result -> assertPage(result, 0, 10, 0))
            .verifyComplete();

        assertEquals(1, appliedParams.size());
        verify(query, never()).fetch();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRejectPolicyReturnsReactiveValidationError() {
        QueryParamEntity source = QueryParamEntity.of();
        source.setPageSize(101);
        AtomicBoolean queryCreated = new AtomicBoolean();

        StepVerifier
            .create(QueryHelper.queryPager(
                source,
                () -> {
                    queryCreated.set(true);
                    return mock(ReactiveQuery.class);
                },
                new PagerQueryPolicy(100, PagerQueryPolicy.OverflowPolicy.REJECT)))
            .verifyErrorSatisfies(error -> {
                assertTrue(error instanceof ValidationException);
                ValidationException validation = (ValidationException) error;
                assertEquals("error.page_size_exceeded", validation.getI18nCode());
                assertEquals("pageSize", validation.getDetails().get(0).getProperty());
            });

        assertFalse(queryCreated.get());
        assertEquals(101, source.getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvalidCustomMaxFailsFast() {
        QueryParamEntity source = QueryParamEntity.of();

        for (int invalidMax : new int[]{0, -1}) {
            try {
                QueryHelper.queryPager(source, () -> mock(ReactiveQuery.class), invalidMax);
                fail("Expected invalid max page size to fail: " + invalidMax);
            } catch (IllegalArgumentException error) {
                assertTrue(error.getMessage().contains("maxPageSize"));
            }
        }
    }

    private static void assertPage(PagerResult<?> result,
                                   int total,
                                   int pageSize,
                                   int dataSize) {
        assertEquals(total, result.getTotal());
        assertEquals(pageSize, result.getPageSize());
        assertEquals(dataSize, result.getData().size());
    }

    @SuppressWarnings("unchecked")
    private static ReactiveQuery<Integer> mockPreparedQuery(Supplier<Mono<Integer>> loader,
                                                           List<QueryOperationContext> scopes) {
        ReactiveQuery<Integer> query = mock(ReactiveQuery.class);
        when(query.setParam(any(QueryParam.class))).thenReturn(query);
        Mono<Integer> preparation = Mono.deferContextual(context -> {
            QueryOperationContext scope = QueryOperationContext.current(context).orElseThrow();
            scopes.add(scope);
            return scope.memoize(new QueryOperationContext.Key<>(Integer.class, "metadata"), loader);
        });
        when(query.count()).thenReturn(preparation);
        when(query.fetch()).thenReturn(preparation.flux());
        return query;
    }

    @SuppressWarnings("unchecked")
    private static ReactiveQuery<Integer> mockQuery(Mono<Integer> count,
                                                    Flux<Integer> data,
                                                    List<QueryParamEntity> appliedParams) {
        ReactiveQuery<Integer> query = mock(ReactiveQuery.class);
        when(query.setParam(any(QueryParam.class))).thenAnswer(invocation -> {
            QueryParam param = invocation.getArgument(0);
            appliedParams.add(QueryParamEntity.of(param));
            return query;
        });
        when(query.count()).thenReturn(count);
        when(query.fetch()).thenReturn(data);
        return query;
    }
}
