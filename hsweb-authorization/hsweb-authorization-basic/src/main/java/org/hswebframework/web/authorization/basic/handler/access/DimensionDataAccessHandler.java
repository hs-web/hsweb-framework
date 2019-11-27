package org.hswebframework.web.authorization.basic.handler.access;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.core.param.Param;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.annotation.DimensionDataAccess;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.simple.DimensionDataAccessConfig;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.utils.AnnotationUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class DimensionDataAccessHandler implements DataAccessHandler {
    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof DimensionDataAccessConfig;
    }

    @Override
    public boolean handle(DataAccessConfig access, AuthorizingContext context) {
        DimensionDataAccessConfig config = ((DimensionDataAccessConfig) access);
        DataAccessHandlerContext requestContext = DataAccessHandlerContext.of(context, config.getScopeType());
        if (!checkSupported(config, requestContext)) {
            return false;
        }
        switch (access.getAction()) {
            case Permission.ACTION_QUERY:
            case Permission.ACTION_GET:
                return doHandleQuery(config, requestContext);
            case Permission.ACTION_ADD:
            case Permission.ACTION_SAVE:
            case Permission.ACTION_UPDATE:
                return doHandleUpdate(config, requestContext);
            case Permission.ACTION_DELETE:
                return doHandleDelete(config, requestContext);
            default:
                if (log.isDebugEnabled()) {
                    log.debug("data access [{}] not support for {}", config.getType().getId(), access.getAction());
                }
                return true;
        }

    }

    @SneakyThrows
    protected String getProperty(DimensionDataAccessConfig cfg,
                                 DataAccessHandlerContext ct) {
        return Optional.ofNullable(
                getMappingInfo(ct).get(cfg.getScopeType()))
                .map(MappingInfo::getProperty)
                .orElseGet(() -> {
                    log.warn("{} not supported dimension data access", ct.getParamContext().getMethod());
                    return null;
                });
    }

    protected boolean checkSupported(DimensionDataAccessConfig cfg, DataAccessHandlerContext ctx) {
        Authentication authentication = ctx.getAuthentication();

        /*
            DataAccessHelper.assert()
         */
        if (CollectionUtils.isEmpty(ctx.getDimensions())) {
            log.warn("user:[{}] dimension not setup", authentication.getUser().getId());
            return false;
        }

        if (!getMappingInfo(ctx).containsKey(cfg.getScopeType())) {
            log.warn("{} not supported dimension data access.see annotation: @DimensionDataAccess", ctx.getParamContext().getMethod());
            return false;
        }

        return true;
    }

    protected boolean doHandleDelete(DimensionDataAccessConfig cfg,
                                     DataAccessHandlerContext context) {


        // TODO: 2019-11-18
        return doHandleUpdate(cfg, context);

    }

    @SuppressWarnings("all")
    protected Object handleUpdateById(DimensionDataAccessConfig config,
                                      DataAccessHandlerContext context,
                                      MappingInfo mappingInfo,
                                      Object id) {
        List<Dimension> dimensions = context.getDimensions();

        Set<Object> scope = CollectionUtils.isNotEmpty(config.getScope()) ?
                config.getScope() :
                dimensions
                        .stream()
                        .map(Dimension::getId)
                        .collect(Collectors.toSet());

        Function<Collection<Object>, Mono<Void>> reactiveCheck = obj -> context
                .getRepository()
                .findById(obj)
                .doOnNext(r -> {
                    Object val = FastBeanCopier.copy(r, new HashMap<>(), FastBeanCopier.include(mappingInfo.getProperty()))
                            .get(mappingInfo.getProperty());
                    if (!StringUtils.isEmpty(val)
                            && !scope.contains(val)) {
                        throw new AccessDenyException();
                    }
                })
                .then();
        if (id instanceof Publisher) {
            if (id instanceof Mono) {
                return ((Mono) id)
                        .flatMap(r -> reactiveCheck.apply(r instanceof Collection ? ((Collection) r) : Collections.singleton(r)))
                        .then((Mono) id);
            }
            if (id instanceof Flux) {
                return ((Flux) id)
                        .collectList()
                        .flatMap(reactiveCheck)
                        .thenMany((Flux) id);
            }
        }
        Collection<Object> idVal = id instanceof Collection ? ((Collection) id) : Collections.singleton(id);

        Object result = context.getParamContext().getInvokeResult();
        if (result instanceof Mono) {
            context.getParamContext()
                    .setInvokeResult(((Mono) result)
                            .flatMap(res -> {
                                return reactiveCheck.apply(idVal).thenReturn(res);
                            }));
        } else if (result instanceof Flux) {
            context.getParamContext()
                    .setInvokeResult(((Flux) result)
                            .flatMap(res -> {
                                return reactiveCheck.apply(idVal).thenReturn(res);
                            }));
        } else {
            // TODO: 2019-11-19 非响应式处理
        }
        return id;
    }

    protected boolean doHandleUpdate(DimensionDataAccessConfig cfg,
                                     DataAccessHandlerContext context) {
        MappingInfo info = getMappingInfo(context).get(cfg.getScopeType());
        if (info != null) {
            if (info.idParamIndex != -1) {
                Object param = context.getParamContext().getArguments()[info.idParamIndex];
                context.getParamContext().getArguments()[info.idParamIndex] = handleUpdateById(cfg, context, info, param);
                return true;
            }
        } else {
            return true;
        }

        boolean reactive = context.getParamContext()
                .handleReactiveArguments(publisher -> {
                    if (publisher instanceof Mono) {
                        return Mono.from(publisher)
                                .flatMap(payload -> applyReactiveUpdatePayload(cfg, info, Collections.singleton(payload), context)
                                        .thenReturn(payload));
                    }
                    if (publisher instanceof Flux) {
                        return Flux.from(publisher)
                                .collectList()
                                .flatMapMany(list ->
                                        applyReactiveUpdatePayload(cfg, info, list, context)
                                                .flatMapIterable(v -> list));
                    }

                    return publisher;
                });

        if (!reactive) {
            applyUpdatePayload(cfg, info, Arrays
                    .stream(context.getParamContext().getArguments())
                    .flatMap(obj -> {
                        if (obj instanceof Collection) {
                            return ((Collection<?>) obj).stream();
                        }
                        return Stream.of(obj);
                    })
                    .filter(Entity.class::isInstance)
                    .collect(Collectors.toSet()), context);

            return true;
        }
        return true;

    }

    protected void applyUpdatePayload(DimensionDataAccessConfig config,
                                      MappingInfo mappingInfo,
                                      Collection<?> payloads,
                                      DataAccessHandlerContext context) {
        List<Dimension> dimensions = context.getDimensions();

        Set<Object> scope = CollectionUtils.isNotEmpty(config.getScope()) ?
                config.getScope() :
                dimensions
                        .stream()
                        .map(Dimension::getId)
                        .collect(Collectors.toSet());

        for (Object payload : payloads) {
            if (!(payload instanceof Entity)) {
                continue;
            }
            if (payload instanceof Param) {
                applyQueryParam(config, context, ((Param) payload));
                continue;
            }
            String property = mappingInfo.getProperty();
            Map<String, Object> map = FastBeanCopier.copy(payload, new HashMap<>(), FastBeanCopier.include(property));
            Object value = map.get(property);
            if (StringUtils.isEmpty(value)) {
                if (dimensions.size() == 1) {
                    map.put(property, dimensions.get(0).getId());
                    FastBeanCopier.copy(map, payload, property);
                }
                continue;
            }
            if (CollectionUtils.isNotEmpty(scope)) {
                if (!scope.contains(value)) {
                    throw new AccessDenyException();
                }
            }
        }
    }

    protected Mono<Void> applyReactiveUpdatePayload(DimensionDataAccessConfig config,
                                                    MappingInfo info,
                                                    Collection<?> payloads,
                                                    DataAccessHandlerContext context) {

        return Mono.fromRunnable(() -> applyUpdatePayload(config, info, payloads, context));
    }

    protected boolean doHandleQuery(DimensionDataAccessConfig cfg, DataAccessHandlerContext requestContext) {
        boolean reactive = requestContext.getParamContext().handleReactiveArguments(publisher -> {
            if (publisher instanceof Mono) {
                return Mono
                        .from(publisher)
                        .flatMap(param -> this
                                .applyReactiveQueryParam(cfg, requestContext, param)
                                .thenReturn(param));
            }

            return publisher;
        });

        if (!reactive) {
            Object[] args = requestContext.getParamContext().getArguments();
            this.applyQueryParam(cfg, requestContext, args);
        }
        return true;
    }

    protected String getTermType(DimensionDataAccessConfig cfg) {
        return "in";
    }

    protected void applyQueryParam(DimensionDataAccessConfig cfg,
                                   DataAccessHandlerContext requestContext,
                                   Param param) {
        Set<Object> scope = CollectionUtils.isNotEmpty(cfg.getScope()) ?
                cfg.getScope() :
                requestContext.getDimensions()
                        .stream()
                        .map(Dimension::getId)
                        .collect(Collectors.toSet());

        QueryParamEntity entity = new QueryParamEntity();
        entity.setTerms(new ArrayList<>(param.getTerms()));
        entity.toNestQuery(query ->
                query.where(
                        getProperty(cfg, requestContext),
                        getTermType(cfg),
                        scope));
        param.setTerms(entity.getTerms());
    }

    protected void applyQueryParam(DimensionDataAccessConfig cfg,
                                   DataAccessHandlerContext requestContext,
                                   Object... params) {
        for (Object param : params) {
            if (param instanceof QueryParam) {
                applyQueryParam(cfg, requestContext, (QueryParam) param);
            }
        }
    }

    protected Mono<Void> applyReactiveQueryParam(DimensionDataAccessConfig cfg,
                                                 DataAccessHandlerContext requestContext,
                                                 Object... param) {


        return Mono.fromRunnable(() -> applyQueryParam(cfg, requestContext, param));
    }

    private Map<Method, Map<String, MappingInfo>> cache = new ConcurrentHashMap<>();


    public Map<String, MappingInfo> getMappingInfo(DataAccessHandlerContext context) {
        return getMappingInfo(ClassUtils.getUserClass(context.getParamContext().getTarget()), context.getParamContext().getMethod());

    }

    private Set<Class<? extends Annotation>> ann = new HashSet<>(Arrays.asList(DimensionDataAccess.class, DimensionDataAccess.Mapping.class));


    private Map<String, MappingInfo> getMappingInfo(Class target, Method method) {

        return cache.computeIfAbsent(method, m -> {
            Set<Annotation> methodAnnotation = AnnotatedElementUtils.findAllMergedAnnotations(method, ann);
            Set<Annotation> classAnnotation = AnnotatedElementUtils.findAllMergedAnnotations(target, ann);


            List<Annotation> all = new ArrayList<>(classAnnotation);
            all.addAll(methodAnnotation);
            if (CollectionUtils.isEmpty(all)) {
                return Collections.emptyMap();
            }
            Map<String, MappingInfo> mappingInfoMap = new HashMap<>();
            for (Annotation annotation : all) {
                if (annotation instanceof DimensionDataAccess) {
                    for (DimensionDataAccess.Mapping mapping : ((DimensionDataAccess) annotation).mapping()) {
                        mappingInfoMap.put(mapping.dimensionType(), MappingInfo.of(mapping));
                    }
                }
                if (annotation instanceof DimensionDataAccess.Mapping) {
                    mappingInfoMap.put(((DimensionDataAccess.Mapping) annotation).dimensionType(), MappingInfo.of(((DimensionDataAccess.Mapping) annotation)));
                }
            }
            return mappingInfoMap;
        });
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class MappingInfo {
        String dimension;

        String property;

        int idParamIndex;

        static MappingInfo of(DimensionDataAccess.Mapping mapping) {
            return new MappingInfo(mapping.dimensionType(), mapping.property(), mapping.idParamIndex());
        }
    }
}
