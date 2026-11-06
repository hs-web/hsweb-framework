package org.hswebframework.web.crud.query;

import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Subscription-local state shared by the count and data queries of one query operation.
 *
 * <p>The operation owner installs this context through {@link #within(Mono)}. It only memoizes
 * successful, immutable preparation results, such as resolved query metadata. It is not a
 * process-wide cache and must not be retained beyond the operation or keyed by individual rows.
 * Concurrent query branches share an in-flight resolution; cancelling all waiting branches
 * cancels an unresolved source. Empty and failed resolutions are not cached.</p>
 *
 * @since 5.0.3
 * @see QueryHelper#queryPager
 */
public final class QueryOperationContext {

    private final ConcurrentMap<Key<?>, Mono<?>> resolutions = new ConcurrentHashMap<>();

    /**
     * Typed identity of one immutable preparation result within an operation.
     * The qualifier must have stable equality for the operation's lifetime.
     *
     * @param valueType result type, included in key equality
     * @param qualifier operation-local discriminator, such as a query metadata request
     * @param <T> preparation result type
     * @since 5.0.3
     */
    public record Key<T>(Class<T> valueType, Object qualifier) {
        public Key {
            Objects.requireNonNull(valueType, "valueType");
            Objects.requireNonNull(qualifier, "qualifier");
        }
    }

    /**
     * Resolve a value once for this operation, preserving the subscribing Reactor context.
     *
     * <p>The first loader for an equal key owns its resolution. Loaders must return cold,
     * non-blocking publishers and immutable values. Errors and empty completion propagate to
     * callers without becoming cached results; a later caller can resolve the key again.</p>
     *
     * @param key typed identity of the preparation
     * @param loader cold preparation source; invoked on subscription, without implicit retry
     * @param <T> preparation result type
     * @return cancellation-aware shared resolution
     * @since 5.0.3
     */
    public <T> Mono<T> memoize(Key<T> key, Supplier<Mono<T>> loader) {
        Mono<?> resolution = resolutions.computeIfAbsent(key, ignored -> Mono
            .defer(loader)
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty())
            // Reactor's value cache converts an empty source into an error. Keep empty as a
            // value only for the current waiting group, and invalidate it for the next caller.
            .cacheInvalidateIf(Optional::isEmpty)
            .flatMap(Mono::justOrEmpty));
        return resolution.map(key.valueType()::cast);
    }

    /**
     * Find the operation scope carried by this subscriber; absence means no scope was installed.
     *
     * @param context subscriber context
     * @return current operation scope, if present
     * @since 5.0.3
     */
    public static Optional<QueryOperationContext> current(ContextView context) {
        return context.getOrEmpty(QueryOperationContext.class);
    }

    /**
     * Run an operation in a new scope per subscription, or join an explicitly enclosing scope.
     *
     * <p>All child subscriptions, including parallel count and data queries, receive the same
     * scope through Reactor context. Re-subscribing without an enclosing scope creates fresh
     * state. No source is subscribed or scheduled by this method.</p>
     *
     * @param operation cold operation to execute
     * @param <T> operation result type
     * @return operation with subscription-scoped preparation state
     * @since 5.0.3
     */
    public static <T> Mono<T> within(Mono<T> operation) {
        return Mono.defer(() -> operation.contextWrite(context -> context.hasKey(QueryOperationContext.class)
            ? context
            : context.put(QueryOperationContext.class, new QueryOperationContext())));
    }
}
