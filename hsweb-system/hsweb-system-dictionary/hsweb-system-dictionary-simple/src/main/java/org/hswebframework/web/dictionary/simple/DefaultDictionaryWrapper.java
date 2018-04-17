package org.hswebframework.web.dictionary.simple;

import lombok.Setter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.dictionary.api.DictionaryInfo;
import org.hswebframework.web.dictionary.api.DictionaryInfoService;
import org.hswebframework.web.dictionary.api.DictionaryWrapper;
import org.hswebframework.web.proxy.Proxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhouhao
 * @since 3.0
 */
public class DefaultDictionaryWrapper implements DictionaryWrapper, DefaultDictionaryHelper {
    static DictionaryWrapperObject EMPTY_WRAPPER = new DictionaryWrapperObject() {
        @Override
        public void wrap(Object id, Object bean, DefaultDictionaryHelper helper) {

        }

        @Override
        public void persistent(Object id, Object bean, DefaultDictionaryHelper helper) {

        }
    };

    @Autowired
    @Setter
    private DictionaryInfoService dictionaryInfoService;

    protected Map<Class, DictionaryWrapperObject> cache = new ConcurrentHashMap<>();

    protected DictionaryWrapperObject createCache(Class bean) {
        String beanName = bean.getName();
        StringBuilder wrapMethod = new StringBuilder()
                .append("public void wrap(Object id,Object bean, org.hswebframework.web.dictionary.simple.DefaultDictionaryHelper helper)")
                .append("{\n")
                .append(bean.getName()).append(" target=(").append(bean.getName()).append(")bean;\n");

        StringBuilder persistentMethod = new StringBuilder()
                .append("public void persistent(Object id,Object bean, org.hswebframework.web.dictionary.simple.DefaultDictionaryHelper helper)")
                .append("{\n")
                .append(bean.getName()).append(" target=(").append(bean.getName()).append(")bean;\n");

        PropertyDescriptor[] descriptors = BeanUtilsBean.getInstance().getPropertyUtils().getPropertyDescriptors(bean);
        boolean hasDict = false;
        for (PropertyDescriptor descriptor : descriptors) {
            Class type = descriptor.getPropertyType();
            boolean isArray = type.isArray();
            if (isArray) {
                type = type.getComponentType();
            }
            //枚举字典并且枚举数量大于64
            if (type.isEnum() && EnumDict.class.isAssignableFrom(type) && type.getEnumConstants().length >= 64) {
                String typeName = isArray ? type.getName().concat("[]") : type.getName();
                String dictId = type.getName();
                Dict dict = (Dict) type.getAnnotation(Dict.class);
                if (dict != null) {
                    dictId = dict.id();
                }
                wrapMethod.append("{\n");
                wrapMethod.append(typeName).append(" dict=(").append(typeName).append(")helper.getDictEnum(id,")
                        .append("\"").append(beanName).append(".").append(descriptor.getName()).append("\"").append(",\"").append(dictId).append("\"")
                        .append(",").append(typeName).append(".class);\n");
                wrapMethod.append("target.").append(descriptor.getWriteMethod().getName()).append("(dict);\n");
                wrapMethod.append("}");

                persistentMethod.append("helper.persistent(id,")
                        .append("\"").append(beanName).append(".").append(descriptor.getName()).append("\"").append(",\"").append(dictId).append("\"")
                        .append(",").append(typeName).append(".class,")
                        .append("target.").append(descriptor.getReadMethod().getName()).append("()")
                        .append(");\n");

                hasDict = true;
            }
        }
        wrapMethod.append("\n}");
        persistentMethod.append("\n}");
        if (hasDict) {
            return Proxy.create(DictionaryWrapperObject.class)
                    .addMethod(wrapMethod.toString())
                    .addMethod(persistentMethod.toString())
                    .newInstance();
        }
        return EMPTY_WRAPPER;
    }

    @Override
    public <T> T wrap(Object id, T bean) {
        cache.computeIfAbsent(ClassUtils.getUserClass(bean.getClass()), this::createCache)
                .wrap(id, bean, getHelper());
        return bean;
    }

    protected DefaultDictionaryHelper getHelper() {
        return this;
    }

    @Override
    public <T> T persistent(Object id, T bean) {
        cache.computeIfAbsent(ClassUtils.getUserClass(bean.getClass()), this::createCache)
                .persistent(id, bean, getHelper());
        return bean;
    }

    @Override
    @SuppressWarnings("all")
    public Object getDictEnum(Object id, String targetKey, String dictId, Class type) {
        List<DictionaryInfo> infos = dictionaryInfoService.select(targetKey, String.valueOf(id), dictId);
        Class componentType = type.isArray() ? type.getComponentType() : type;
        if (componentType.isEnum() && EnumDict.class.isAssignableFrom(componentType)) {
            Stream stream = infos.stream()
                    .map(DictionaryInfo::getValue)
                    .map(val -> EnumDict.find(componentType, val).orElse(null))
                    .filter(Objects::nonNull);
            if (type.isArray()) {
                return stream.toArray(len -> Array.newInstance(componentType, len));
            } else {
                return stream.findFirst().orElse(null);
            }
        }
        return null;
    }

    @Override
    public void persistent(Object id, String targetKey, String dictId, Class type, Object value) {
        if (value == null) {
            return;
        }
        Class componentType = type.isArray() ? type.getComponentType() : type;
        if (componentType.isEnum() && EnumDict.class.isAssignableFrom(componentType)) {
            List<EnumDict> dicts;
            if (type.isArray()) {
                dicts = Arrays.asList(((EnumDict[]) value));
            } else {
                dicts = Arrays.asList(((EnumDict) value));
            }
            dictionaryInfoService.delete(targetKey, String.valueOf(id), dictId);
            dictionaryInfoService.insert(dicts.stream().map(dict ->
                    DictionaryInfo.builder()
                            .value(String.valueOf(dict.getValue()))
                            .dictionaryId(dictId)
                            .text(dict.getText())
                            .targetKey(targetKey)
                            .targetId(String.valueOf(id))
                            .build()).collect(Collectors.toList()));
        }

    }
}
