package org.hswebframework.web.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.validator.LogicPrimaryKey;
import org.hswebframework.web.validator.LogicPrimaryKeyValidator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("all")
@Slf4j
public class DefaultLogicPrimaryKeyValidator implements LogicPrimaryKeyValidator {

    private static final Map<Class, Map<Class, Validator>> validatorCache = new HashMap<>();


    private static final DefaultLogicPrimaryKeyValidator instrance = new DefaultLogicPrimaryKeyValidator();

    protected DefaultLogicPrimaryKeyValidator() {
    }

    public static DefaultLogicPrimaryKeyValidator getInstrance() {
        return instrance;
    }

    private static final Validator ALWAYS_PASSED_VALIDATOR = bean -> {
        return Result.passed();
    };

    public static <T> void registerQuerySuppiler(Class<T> type, Function<T, Query<T, QueryParamEntity>> querySupplier) {
        validatorCache.computeIfAbsent(type, instrance::createValidator)
                .values()
                .stream()
                .filter(DefaultValidator.class::isInstance)
                .map(DefaultValidator.class::cast)
                .forEach(validator -> validator.querySupplier = querySupplier);
    }

    @Override
    public Result validate(Object bean, Class... groups) {

        Class target = ClassUtils.getUserClass(bean);
        Result result;
        if (null != groups && groups.length > 0) {
            result = Arrays.stream(groups)
                    .map(group ->
                            validatorCache.computeIfAbsent(target, this::createValidator)
                                    .getOrDefault(group, ALWAYS_PASSED_VALIDATOR)
                                    .doValidate(bean))
                    .filter(Result::isError)
                    .findFirst()
                    .orElseGet(Result::passed);

        } else {
            result = validatorCache.computeIfAbsent(target, this::createValidator)
                    .getOrDefault(Void.class, ALWAYS_PASSED_VALIDATOR)
                    .doValidate(bean);
        }
        return result;

    }

    protected Map<Class, Validator> createValidator(Class target) {
        //属性名:注解
        Map<String, LogicPrimaryKey> keys = new HashMap<>();

        ReflectionUtils.doWithFields(target, field -> {
            LogicPrimaryKey primaryKey = field.getAnnotation(LogicPrimaryKey.class);
            if (primaryKey != null) {
                keys.put(field.getName(), primaryKey);
            }
        });

        //获取类上的注解
        Class[] tempClass = new Class[]{target};
        LogicPrimaryKey classAnn = null;
        Class[] empty = new Class[0];

        while (tempClass.length != 0) {

            for (Class group : tempClass) {
                classAnn = AnnotationUtils.findAnnotation(group, LogicPrimaryKey.class);
                if (null != classAnn) {
                    if (classAnn.value().length > 0) {
                        for (String field : classAnn.value()) {
                            keys.put(field, classAnn);
                        }
                        tempClass = empty;
                        continue;
                    } else {
                        //如果注解没有指定字段则从group中获取
                        tempClass = classAnn.groups();
                        if (tempClass.length == 1 && tempClass[0] == Void.class) {
                            log.warn("类{}的注解{}无效,请设置value属性或者group属性", classAnn, tempClass);
                            continue;
                        }
                    }
                } else {
                    tempClass = empty;
                    continue;
                }
            }
        }

        if (keys.isEmpty()) {
            return new java.util.HashMap<>();
        }
        return keys.entrySet()
                .stream()
                .flatMap(entry -> Stream.of(entry.getValue().groups())
                        .flatMap(group -> Optional.ofNullable(entry.getValue().value())
                                .map(Arrays::asList)
                                .filter(CollectionUtils::isNotEmpty)
                                .orElse(Arrays.asList(entry.getKey()))
                                .stream()
                                .map(field -> LogicPrimaryKeyField.builder()
                                        .field(field)
                                        .termType(entry.getValue().termType())
                                        .condition(entry.getValue().condition())
                                        .matchNullOrEmpty(entry.getValue().matchNullOrEmpty())
                                        .group(group)
                                        .build())
                        ))
                .collect(Collectors.groupingBy(
                        //按group分组
                        LogicPrimaryKeyField::getGroup,
                        //将每一组的集合构造为验证器对象
                        Collectors.collectingAndThen(
                                Collectors.mapping(Function.identity(), Collectors.toSet())
                                , list -> DefaultValidator.builder()
                                        .infos(list)
                                        .targetType(target)
                                        .build())
                        )
                );

    }

    interface Validator<T> {
        Result doValidate(T bean);
    }

    @Builder
    static class DefaultValidator<T> implements Validator<T> {
        private Set<LogicPrimaryKeyField> infos = new HashSet<>();

        private Class<T> targetType;

        private volatile Function<T, Query<T, QueryParamEntity>> querySupplier;

        public Result doValidate(T bean) {
            if (querySupplier == null) {
                log.warn("未设置查询函数," +
                                "你可以在服务初始化的时候通过调用" +
                                "DefaultLogicPrimaryKeyValidator" +
                                ".registerQuerySuppiler({},bean -> this.createQuery().not(\"id\", bean.getId()))" +
                                "进行设置"
                        , targetType);
                return Result.passed();
            }

            Query<T, QueryParamEntity> query = querySupplier.apply(bean);

            //转为map
            Map<String, Object> mapBean = FastBeanCopier.copy(bean, new HashMap<>());

            Map<String, Object> properties = new HashMap<>();

            for (LogicPrimaryKeyField info : infos) {
                String field = info.getField();
                Object value = mapBean.get(field);
                //为空,可能是有字段嵌套,也有可能是真的null
                if (value == null) {
                    String tmpField = field;
                    Object tmpValue = null;
                    Map<String, Object> tempMapBean = mapBean;
                    //嵌套的场景
                    while (tmpValue == null && tmpField.contains(".")) {
                        String[] nest = tmpField.split("[.]", 2);
                        Object nestObject = tempMapBean.get(nest[0]);
                        if (nestObject == null) {
                            break;
                        }
                        if (nestObject instanceof Map) {
                            tempMapBean = ((Map) nestObject);
                        } else {
                            tempMapBean = FastBeanCopier.copy(nestObject, new HashMap<>());
                        }
                        tmpField = nest[1];
                        tmpValue = tempMapBean.get(tmpField);
                    }
                    value = tmpValue;
                }
                if (StringUtils.isEmpty(value)) {
                    if (info.matchNullOrEmpty) {
                        if (value == null) {
                            query.isNull(info.getField());
                        } else {
                            query.isEmpty(info.getField());
                        }
                    }
                } else {
                    String termType = StringUtils.isEmpty(info.termType) ? TermType.eq : info.termType;
                    query.and(info.getField(), termType, value);
                }
                properties.put(info.getField(), value);
            }

            T result = query.single();

            if (result != null) {
                Result validateResult = new Result();
                validateResult.setError(true);
                validateResult.setData(result);
                validateResult.setProperties(properties);
                return validateResult;
            }
            return Result.passed();
        }
    }

    @Getter
    @Setter
    @Builder
    private static class LogicPrimaryKeyField {
        private String field;

        private String condition;

        private boolean matchNullOrEmpty;

        private String termType;

        private Class group;

        @Override
        public int hashCode() {
            return field.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LogicPrimaryKeyField) {
                return hashCode() == obj.hashCode();
            }
            return false;
        }
    }
}
