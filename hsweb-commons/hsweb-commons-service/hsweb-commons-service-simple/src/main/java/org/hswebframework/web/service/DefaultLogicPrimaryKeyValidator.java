package org.hswebframework.web.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.validator.DuplicateKeyException;
import org.hswebframework.web.validator.LogicPrimaryKey;
import org.hswebframework.web.validator.LogicPrimaryKeyValidator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Slf4j
public class DefaultLogicPrimaryKeyValidator implements LogicPrimaryKeyValidator {

    private static final Map<Class, Map<Class, Validator>> validatorCache = new HashMap<>();


    private static final Validator EMPTY_VALIDATOR = bean -> {
    };

    public <T> void registerQuerySuppiler(Class type, Function<T, Query<T, QueryParamEntity>> querySupplier) {
        validatorCache.computeIfAbsent(type, this::createValidator)
                .values()
                .stream()
                .filter(DefaultValidator.class::isInstance)
                .map(DefaultValidator.class::cast)
                .forEach(validator -> validator.querySupplier = querySupplier);
    }

    @Override
    public void validate(Object bean, Class... groups) {

        Class target = ClassUtils.getUserClass(bean);
        if (null != groups && groups.length > 0) {
            for (Class group : groups) {
                validatorCache.computeIfAbsent(target, this::createValidator)
                        .getOrDefault(group, EMPTY_VALIDATOR)
                        .doValidate(bean);
            }
        } else {
            validatorCache.computeIfAbsent(target, this::createValidator)
                    .getOrDefault(Void.class, EMPTY_VALIDATOR)
                    .doValidate(bean);
        }

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
        Class tempClass = target;
        LogicPrimaryKey classAnn = null;

        while (classAnn == null) {
            classAnn = AnnotationUtils.findAnnotation(tempClass, LogicPrimaryKey.class);
            if (null != classAnn) {
                if (classAnn.value().length > 0) {
                    for (String field : classAnn.value()) {
                        keys.put(field, classAnn);
                    }
                    break;
                } else {
                    //如果注解没有指定字段则从group中获取
                    tempClass = classAnn.group();
                    if (tempClass == Void.class) {
                        log.warn("类{}的注解{}无效,请设置value属性或者group属性", classAnn, tempClass);
                        break;
                    }
                }
            } else {
                break;
            }
        }

        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        return keys.entrySet()
                .stream()
                .collect(
                        Collectors.groupingBy(

                                //按注解中的group分组
                                e -> e.getValue().group()
                                //将分组后的注解转为字段配置
                                , Collectors.collectingAndThen(
                                        Collectors.mapping(e -> LogicPrimaryKeyField
                                                .builder()
                                                .field(e.getKey())
                                                .termType(e.getValue().termType())
                                                .condition(e.getValue().condition())
                                                .matchNullOrEmpty(e.getValue().matchNullOrEmpty())
                                                .build(), Collectors.toList())
                                        //将每一组的字段集合构造为验证器对象
                                        , list -> DefaultValidator
                                                .builder()
                                                .infos(list)
                                                .build())
                        )
                );

    }

    interface Validator<T> {
        void doValidate(T bean);
    }

    @Builder
    static class DefaultValidator<T> implements Validator<T> {
        private List<LogicPrimaryKeyField> infos = new ArrayList<>();

        private volatile Function<T, Query<T, QueryParamEntity>> querySupplier;

        public void doValidate(T bean) {
            if (querySupplier == null) {
                return;
            }

            Query<T, QueryParamEntity> query = querySupplier.apply(bean);

            Map<String, Object> mapBean = FastBeanCopier.copy(bean, new HashMap<>());

            for (LogicPrimaryKeyField info : infos) {
                String field = info.getField();
                Object value = mapBean.get(field);
                if (value == null) {
                    String tmpField = field;
                    Object tmpValue = null;
                    Map<String, Object> tempMapBean = mapBean;
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
            }

            T result = query.single();

            if (result != null) {

                throw new DuplicateKeyException(result, "存在重复数据");
            }
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
    }
}
