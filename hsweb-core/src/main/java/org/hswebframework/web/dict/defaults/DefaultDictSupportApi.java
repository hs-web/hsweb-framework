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
        return target;
    }

    @Override
    @SuppressWarnings("all")
    public <T> T wrap(T target) {
        return target;
    }
}
