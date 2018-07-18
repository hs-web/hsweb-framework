package org.hswebframework.web.dict.defaults;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.dict.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class DefaultDictDefineRepository implements DictDefineRepository {
    protected static final Map<String, DictDefine> parsedDict = new HashMap<>();

    public static void registerDefine(DictDefine define) {
        parsedDict.put(define.getId(), define);
    }

    @SuppressWarnings("all")
    public static <T extends Enum & EnumDict> ClassDictDefine parseEnumDict(Class<T> type) {
        log.debug("parse enum dict :{}", type);

        Dict dict = type.getAnnotation(Dict.class);

        DefaultClassDictDefine define = new DefaultClassDictDefine();
        define.setField("");
        if (dict != null) {
            define.setId(dict.id());
            define.setParserId(dict.parserId());
            define.setComments(dict.comments());
            define.setAlias(dict.alias());
        } else {
            define.setId(type.getSimpleName());
            define.setAlias(type.getName());
            define.setComments(type.getSimpleName());
        }

        List dicts = Arrays.asList(type.getEnumConstants());

        define.setItems(new ArrayList<>(dicts));

        return define;

    }

    @Override
    public DictDefine getDefine(String id) {
        return parsedDict.get(id);
    }

    @Override
    public List<DictDefine> getAllDefine() {
        return new ArrayList<>(parsedDict.values());
    }

    @Override
    public List<ClassDictDefine> getDefine(Class type) {
        return new java.util.ArrayList<>();
    }


    @Override
    public void addDefine(DictDefine dictDefine) {
        registerDefine(dictDefine);
    }
}
