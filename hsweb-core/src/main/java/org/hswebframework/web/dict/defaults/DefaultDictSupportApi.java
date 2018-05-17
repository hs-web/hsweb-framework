package org.hswebframework.web.dict.defaults;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.hswebframework.web.dict.ClassDictDefine;
import org.hswebframework.web.dict.DictDefineRepository;
import org.hswebframework.web.dict.DictParser;
import org.hswebframework.web.dict.DictSupportApi;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class DefaultDictSupportApi implements DictSupportApi {

    private DictDefineRepository repository;

    private Map<String, DictParser> parserRepo = new HashMap<>();

    public DefaultDictSupportApi(DictDefineRepository repository) {
        this.repository = repository;
        DictParser defaultParser = new DefaultDictParser();
        parserRepo.put(defaultParser.getId(), defaultParser);
    }

    @Override
    public DictParser getParser(String id, String defaultId) {
        return Optional.ofNullable(parserRepo.get(id)).orElseGet(() -> parserRepo.get(defaultId));
    }

    @Override
    @SuppressWarnings("all")
    public <T> T unwrap(T target) {
        if (target == null) {
            return null;
        }
        if (target instanceof Map) {
            return target;
        }
        if (target instanceof List) {
            return (T) ((List) target).stream()
                    .map(this::wrap)
                    .collect(Collectors.toList());
        }
        if (target instanceof String || target.getClass().isEnum() || target.getClass().isPrimitive() || target instanceof Type) {
            return target;
        }

        Class type = ClassUtils.getUserClass(target);
        List<ClassDictDefine> defines = repository.getDefine(type);
        if (defines.isEmpty()) {
            return target;
        }
        for (ClassDictDefine define : defines) {
            String fieldName = define.getField();
            String alias = define.getAlias();
            if (StringUtils.isEmpty(alias)) {
                continue;
            }
            try {
                Object fieldValue = BeanUtils.getProperty(target, fieldName);
                if (fieldValue != null) {
                    continue;
                }
                Object value = BeanUtils.getProperty(target, alias);
                if (value == null) {
                    continue;
                }
                BeanUtils.setProperty(target, fieldName, getParser(define.getParserId()).parseValue(define, String.valueOf(value)));
            } catch (Exception e) {
                log.warn("unwrap error", e.getMessage());
            }
        }
        return target;
    }

    @Override
    @SuppressWarnings("all")
    public <T> T wrap(T target) {
        if (target == null) {
            return null;
        }
        if (target instanceof Map) {
            return target;
        }
        if (target instanceof List) {
            return (T) ((List) target).stream().map(this::wrap).collect(Collectors.toList());
        }
        if (target instanceof String || target.getClass().isEnum() || target.getClass().isPrimitive() || target instanceof Type) {
            return target;
        }
        Class type = ClassUtils.getUserClass(target);
        List<ClassDictDefine> defines = repository.getDefine(type);
        if (defines.isEmpty()) {
            return target;
        }
        for (ClassDictDefine define : defines) {
            String fieldName = define.getField();
            String alias = define.getAlias();
            if (StringUtils.isEmpty(alias)) {
                continue;
            }
            try {
                Object value = BeanUtils.getProperty(target, fieldName);
                if (value == null) {
                    continue;
                }
                BeanUtils.setProperty(target, alias, getParser(define.getParserId()).parseText(define, String.valueOf(value)));
            } catch (Exception e) {
                log.warn("wrap error", e.getMessage());
            }
        }
        return target;
    }
}
