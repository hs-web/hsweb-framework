package org.hswebframework.web.crud.configuration;

import org.hswebframework.ezorm.rdb.mapping.parser.EntityTableMetadataParser;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class CompositeEntityTableMetadataResolver implements EntityTableMetadataResolver {

    private final List<EntityTableMetadataParser> resolvers = new ArrayList<>();

    private final Map<Class<?>, AtomicReference<RDBTableMetadata>> cache = new ConcurrentHashMap<>();

    public void addParser(EntityTableMetadataParser resolver) {
        resolvers.add(resolver);
    }

    @Override
    public RDBTableMetadata resolve(Class<?> entityClass) {

        return cache.computeIfAbsent(entityClass, type -> new AtomicReference<>(doResolve(type))).get();
    }

    private RDBTableMetadata doResolve(Class<?> entityClass) {
        return resolvers
            .stream()
                .map(resolver -> resolver.parseTableMetadata(entityClass))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce((t1, t2) -> {
                    t2.merge(t1);
                    return t2;
                }).orElse(null);
    }
}
