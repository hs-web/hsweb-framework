package org.hswebframework.web.boost.bean;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.hswebframework.web.boost.Compiler;
import org.springframework.util.ClassUtils;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhouhao
 * @since 3.0
 */
public class FastBeanCopier {
    private static final Map<String, Copier> CACHE = new HashMap<>();

    private static final Map<String, Map<String, String>> PROPERTY_MAPPING = new HashMap<>();

    private static final PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();

    private static final Map<Class, Class> wrapperClassMapping = new HashMap<>();

    public static final DefaultConvert DEFAULT_CONVERT = new DefaultConvert();

    static {
        wrapperClassMapping.put(byte.class, Byte.class);
        wrapperClassMapping.put(short.class, Short.class);
        wrapperClassMapping.put(int.class, Integer.class);
        wrapperClassMapping.put(float.class, Float.class);
        wrapperClassMapping.put(double.class, Double.class);
        wrapperClassMapping.put(char.class, Character.class);
        wrapperClassMapping.put(boolean.class, Boolean.class);
        wrapperClassMapping.put(long.class, Long.class);
    }

    public static void copy(Object source, Object target, String... ignore) {
        copy(source, target, DEFAULT_CONVERT, ignore);
    }

    public static Copier getCopier(Object source, Object target, boolean autoCreate) {
        Class sourceType = ClassUtils.getUserClass(source);
        Class targetType = ClassUtils.getUserClass(target);
        String key = createCacheKey(sourceType, targetType);
        if (autoCreate) {
            return CACHE.computeIfAbsent(key, k -> createCopier(sourceType, targetType));
        } else {
            return CACHE.get(key);
        }

    }

    public static void copy(Object source, Object target, Converter converter, String... ignore) {
        getCopier(source, target, true)
                .copy(source, target, (ignore == null || ignore.length == 0) ? new HashSet<>() : new HashSet<>(Arrays.asList(ignore)), converter);
    }

    private static String createCacheKey(Class source, Class target) {
        return source.getName().concat("=>").concat(target.getName());
    }

    public static Copier createCopier(Class source, Class target) {
        String method = "public void copy(Object s, Object t, java.util.Set ignore, " +
                "org.hswebframework.web.boost.bean.Converter converter){\n" +
                source.getName() + " source=(" + source.getName() + ")s;\n" +
                target.getName() + " target=(" + target.getName() + ")t;\n" +
                createCopierCode(source, target) +
                "\n}";
        System.out.println(method);
        return Compiler.create(Copier.class)
                .addMethod(method)
                .newInstance();
    }

    private static String createFieldCopierCode(PropertyDescriptor source, PropertyDescriptor target) {
        Method sourceRead = source.getReadMethod();
        if (sourceRead == null) {
            //源对象的get方法不存在
            return "";
        }
        Method targetWrite = target.getWriteMethod();
        if (targetWrite == null) {
            return "";
        }

        PropertyCopierGenerator generator = new PropertyCopierGenerator();
        generator.target = target;
        generator.source = source;

        return generator.generate();
    }

    private static String createCopierCode(Class source, Class target) {
        Map<String, PropertyDescriptor> sourceCache = Stream.of(propertyUtils.getPropertyDescriptors(source))
                .collect(Collectors.toMap(FeatureDescriptor::getName, Function.identity()));
        StringBuilder builder = new StringBuilder();

        Arrays.asList(propertyUtils.getPropertyDescriptors(target))
                .forEach((targetField) -> {
                    PropertyDescriptor sourceField = sourceCache.get(targetField.getName());
                    if (null != sourceField) {
                        builder.append(createFieldCopierCode(sourceField, targetField))
                                .append("\n");
                    } else {
                        //源字段不存
                    }
                });

        return builder.toString();
    }

    static class PropertyCopierGenerator {
        PropertyDescriptor source;
        PropertyDescriptor target;
        List<String> lines = new ArrayList<>();

        private boolean targetIsPrimitive() {
            return target.getPropertyType().isPrimitive();
        }

        private boolean sourceIsPrimitive() {
            return source.getPropertyType().isPrimitive();
        }

        private boolean typeIsWrapper(Class type) {
            return wrapperClassMapping.values().contains(type);
        }

        private Class getPrimitiveType(Class type) {
            return wrapperClassMapping.entrySet().stream()
                    .filter(entry -> entry.getValue() == type)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
        }

        private String getReadSourceObjectValueCode() {
            if (sourceIsPrimitive()) {
                Class wrapperClass = wrapperClassMapping.get(source.getPropertyType());
                return wrapperClass.getName() + ".valueOf(source." + source.getReadMethod().getName() + "())";
            }

            return "source." + source.getReadMethod().getName() + "()";
        }

        private boolean notNull() {
            return !sourceIsPrimitive();
        }

        private void generateConvert() {
            StringBuilder convertCode = new StringBuilder();
            convertCode.append("if(!ignore.contains(\"").append(target.getName())
                    .append("\")");
            if (notNull()) {
                convertCode.append("&&source.")
                        .append(source.getReadMethod().getName())
                        .append("()!=null");
            }
            String targetTypeName = target.getPropertyType().getName();
            if (target.getPropertyType().isArray()) {
                targetTypeName = target.getPropertyType().getComponentType().getName() + "[]";
            }
            convertCode.append("){\n");
            convertCode.append(targetTypeName)
                    .append(" ")
                    .append(target.getName()).append("=");
            String convert = "converter.convert((Object)(" + getReadSourceObjectValueCode() + "),"
                    + target.getPropertyType().getName() + ".class)";
            if (source.getPropertyType() != target.getPropertyType()) {
                if (targetIsPrimitive()) {
                    boolean sourceIsWrapper = typeIsWrapper(source.getPropertyType());
                    Class targetWrapperClass = wrapperClassMapping.get(target.getPropertyType());

                    Class sourcePrimitive = getPrimitiveType(source.getPropertyType());
                    if (sourceIsWrapper) {
                        convertCode.append(target.getPropertyType().getName())
                                .append(".valueOf(")
                                .append(getReadSourceObjectValueCode())
                                .append(".")
                                .append(sourcePrimitive.getName())
                                .append("Value());");
                    } else {
                        convertCode.append("((").append(targetWrapperClass.getName())
                                .append(")")
                                .append(convert)
                                .append(").")
//                                .append(target.getPropertyType().getName())
//                                .append("Value()).")
                                .append(target.getPropertyType().getName())
                                .append("Value();");
                    }
//                    convertCode.append(getReadSourceObjectValueCode())
//                            .append(".")
//                            .append(target.getPropertyType().getName()).append("Value();");

                } else if (sourceIsPrimitive()) {
                    boolean targetIsWrapper = typeIsWrapper(target.getPropertyType());
                    Class targetPrimitive = getPrimitiveType(target.getPropertyType());
                    if (targetIsWrapper) {
                        convertCode.append(target.getPropertyType().getName())
                                .append(".valueOf(")
                                .append(getReadSourceObjectValueCode())
                                .append(".")
                                .append(targetPrimitive.getName())
                                .append("Value());");
                    } else {
                        convertCode.append("(").append(target.getPropertyType().getName())
                                .append(")(")
                                .append(convert)
                                .append(");");
                    }
                } else {
                    convertCode.append("(").append(target.getPropertyType().getName())
                            .append(")(")
                            .append(convert)
                            .append(");");
                }
            } else {
                convertCode.append("source.")
                        .append(source.getReadMethod().getName())
                        .append("();");
            }
            convertCode.append("\ntarget.").append(target.getWriteMethod().getName()).append("(").append(target.getName()).append(");");
            convertCode.append("\n}");
            lines.add(convertCode.toString());
        }

        public String generate() {
            generateConvert();
            return String.join("\n", lines.toArray(new String[lines.size()]));
        }
    }

    static class DefaultConvert implements Converter {

        @Override
        public <T> T convert(Object source, Class<T> targetClass) {
            if (source == null) {
                return null;
            }
            if (targetClass == String.class) {
                return (T) String.valueOf(source);
            }

            org.apache.commons.beanutils.Converter converter = BeanUtilsBean
                    .getInstance()
                    .getConvertUtils()
                    .lookup(targetClass);
            if (null != converter) {
                return converter.convert(targetClass, source);
            }
            try {
                T newTarget = targetClass.newInstance();
                copy(source, newTarget);
                return newTarget;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
