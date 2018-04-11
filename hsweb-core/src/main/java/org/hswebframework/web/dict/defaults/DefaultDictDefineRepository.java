package org.hswebframework.web.dict.defaults;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.dict.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class DefaultDictDefineRepository implements DictDefineRepository {
    protected Map<String, DictDefine> parsedDict = new HashMap<>();

    public void registerDefine(DictDefine define) {
        parsedDict.put(define.getId(), define);
    }

    static {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        SimpleMetadataReaderFactory readerFactory = new SimpleMetadataReaderFactory();

        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath*:org/hswebframework/web/**/*.class");
            for (Resource resource : resources) {
                try {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    Class type = Class.forName(className);
                    if (type.isEnum() && EnumDict.class.isAssignableFrom(type)) {

                    }
                } catch (Error e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected <T extends Enum & EnumDict> List<DictDefine> parseEnumDict(Class<T> type) {
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

        List<ItemDefine> items = new ArrayList<>();

        for (T t : type.getEnumConstants()) {
            items.add(DefaultItemDefine.builder()
                    .text(t.getText())
                    .comments(t.getComments())
                    .value(String.valueOf(t.getValue()))
                    .build());

        }
        define.setItems(items);

        return new ArrayList<>(Collections.singletonList(define));

    }

    @Override
    public DictDefine getDefine(String id) {
        return parsedDict.get(id);
    }

    private List<Field> parseField(Class type) {
        if (type == Object.class) {
            return Collections.emptyList();
        }
        List<Field> fields = new ArrayList<>();
        ReflectionUtils.doWithFields(type, fields::add);
        return fields;
    }

    @Override
    public List<ClassDictDefine> getDefine(Class type) {
        return this.parseDefine(type);
    }

    protected List<ClassDictDefine> parseDefine(Class type) {
        List<ClassDictDefine> defines = new ArrayList<>();

        if (type.isEnum() && EnumDict.class.isAssignableFrom(type)) {
            return parseEnumDict(type);
        }
        for (Field field : parseField(type)) {
            Dict dict = field.getAnnotation(Dict.class);
            if (dict == null) {
                continue;
            }
            String id = dict.id();
            DictDefine dictDefine = getDefine(id);
            if (dictDefine instanceof ClassDictDefine) {
                defines.add(((ClassDictDefine) dictDefine));
            } else {
                DefaultClassDictDefine define;
                if (dictDefine != null) {
                    List<ItemDefine> items = dictDefine.getItems()
                            .stream()
                            .map(item -> DefaultItemDefine.builder()
                                    .text(item.getText())
                                    .value(item.getValue())
                                    .comments(String.join(",", item.getComments()))
                                    .build())
                            .collect(Collectors.toList());
                    define = DefaultClassDictDefine.builder()
                            .id(id)
                            .alias(dictDefine.getAlias())
                            .comments(dictDefine.getComments())
                            .field(field.getName())
                            .items(items)
                            .build();

                } else {
                    List<ItemDefine> items = Arrays
                            .stream(dict.items())
                            .map(item -> DefaultItemDefine.builder()
                                    .text(item.text())
                                    .value(item.value())
                                    .comments(String.join(",", item.comments()))
                                    .build()).collect(Collectors.toList());
                    define = DefaultClassDictDefine.builder()
                            .id(id)
                            .alias(dict.alias())
                            .comments(dict.comments())
                            .field(field.getName())
                            .items(items)
                            .build();
                }
                defines.add(define);
            }
        }
        return defines;
    }
}
