package org.hswebframework.web.dict.defaults;

import org.hswebframework.web.dict.*;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
public class DefaultDictDefineRepository implements DictDefineRepository {
    protected Map<String, DictDefine> parsedDict = new HashMap<>();

    public void registerDefine(DictDefine define) {
        parsedDict.put(define.getId(), define);
    }

    @Override
    public DictDefine getDefine(String id) {
        return parsedDict.get(id);
    }

    private List<Field> parseField(Class type) {
        if (type == Object.class) {
            return Collections.emptyList();
        }
        List<Field> fields=new ArrayList<>();
        ReflectionUtils.doWithFields(type, fields::add);
        return fields;
    }

    @Override
    public List<ClassDictDefine> getDefine(Class type) {
        return this.parseDefine(type);
    }

    protected List<ClassDictDefine> parseDefine(Class type) {
        List<ClassDictDefine> defines = new ArrayList<>();

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
