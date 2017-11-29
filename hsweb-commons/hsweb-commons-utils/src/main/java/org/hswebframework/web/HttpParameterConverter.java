package org.hswebframework.web;

import org.apache.commons.beanutils.BeanMap;
import org.hswebframework.utils.time.DateFormatter;

import java.util.*;
import java.util.function.Function;

public class HttpParameterConverter {

    private Map<String, Object> beanMap;

    private Map<String, String> parameter = new HashMap<>();

    private String prefix = "";

    private static final Map<Class, Function<Object, String>> convertMap = new HashMap<>();

    private static Function<Object, String> defaultConvert = String::valueOf;

    private static final Set<Class> basicClass = new HashSet<>();

    static {
        basicClass.add(int.class);
        basicClass.add(double.class);
        basicClass.add(float.class);
        basicClass.add(byte.class);
        basicClass.add(short.class);
        basicClass.add(char.class);
        basicClass.add(boolean.class);

        basicClass.add(Integer.class);
        basicClass.add(Double.class);
        basicClass.add(Float.class);
        basicClass.add(Byte.class);
        basicClass.add(Short.class);
        basicClass.add(Character.class);
        basicClass.add(String.class);
        basicClass.add(Boolean.class);

        basicClass.add(Date.class);


        putConvert(Date.class, (date) -> DateFormatter.toString(date, "yyyy-MM-dd HH:mm:ss"));


    }

    @SuppressWarnings("unchecked")
    private static <T> void putConvert(Class<T> type, Function<T, String> convert) {
        convertMap.put(type, (Function) convert);

    }

    private String convertValue(Object value) {
        return convertMap.getOrDefault(value.getClass(), defaultConvert).apply(value);
    }

    @SuppressWarnings("unchecked")
    public HttpParameterConverter(Object bean) {
        if (bean instanceof Map) {
            beanMap = ((Map) bean);
        } else {
            beanMap = new HashMap<>((Map) new BeanMap(bean));
            beanMap.remove("class");
            beanMap.remove("declaringClass");
        }
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private void doConvert(String key, Object value) {
        if (value == null) {
            return;
        }
        if(value instanceof Class){
            return;
        }
        Class type = org.springframework.util.ClassUtils.getUserClass(value);

        if (basicClass.contains(type) || value instanceof Number || value instanceof Enum) {
            parameter.put(getParameterKey(key), convertValue(value));
            return;
        }

        if (value instanceof Object[]) {
            value = Arrays.asList(((Object[]) value));
        }

        if (value instanceof Collection) {
            Collection coll = ((Collection) value);
            int count = 0;
            for (Object o : coll) {
                doConvert(key + "[" + count++ + "]", o);
            }
        } else {
            HttpParameterConverter converter = new HttpParameterConverter(value);
            converter.setPrefix(getParameterKey(key).concat("."));
            parameter.putAll(converter.convert());
        }
    }

    private void doConvert() {
        beanMap.forEach(this::doConvert);
    }


    private String getParameterKey(String property) {
        return prefix.concat(property);
    }

    public Map<String, String> convert() {
        doConvert();

        return parameter;
    }

}
