package org.hswebframework.web.bean;

import lombok.Getter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.hswebframework.ezorm.core.Extendable;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FastBeanCopierPropertySupport {
    private static final PropertyUtilsBean PROPERTY_UTILS = BeanUtilsBean.getInstance().getPropertyUtils();

    private static final Map<Class<?>, Class<?>> WRAPPER_CLASS_MAPPING = new HashMap<>();

    static {
        WRAPPER_CLASS_MAPPING.put(byte.class, Byte.class);
        WRAPPER_CLASS_MAPPING.put(short.class, Short.class);
        WRAPPER_CLASS_MAPPING.put(int.class, Integer.class);
        WRAPPER_CLASS_MAPPING.put(float.class, Float.class);
        WRAPPER_CLASS_MAPPING.put(double.class, Double.class);
        WRAPPER_CLASS_MAPPING.put(char.class, Character.class);
        WRAPPER_CLASS_MAPPING.put(boolean.class, Boolean.class);
        WRAPPER_CLASS_MAPPING.put(long.class, Long.class);
    }

    private FastBeanCopierPropertySupport() {
    }

    static String createCopierCode(Class<?> source, Class<?> target) {
        Map<String, ClassProperty> sourceProperties = null;
        Map<String, ClassProperty> targetProperties = null;

        boolean targetIsExtendable = Extendable.class.isAssignableFrom(target);
        boolean sourceIsExtendable = Extendable.class.isAssignableFrom(source);
        boolean targetIsMap = Map.class.isAssignableFrom(target);
        boolean sourceIsMap = Map.class.isAssignableFrom(source);
        if (sourceIsMap) {
            if (!targetIsMap) {
                targetProperties = createProperty(target);
                sourceProperties = createMapProperty(targetProperties);
            }
        } else if (targetIsMap) {
            sourceProperties = createProperty(source);
            targetProperties = createMapProperty(sourceProperties);
        } else {
            targetProperties = createProperty(target);
            sourceProperties = createProperty(source);
        }
        if (sourceProperties == null || targetProperties == null) {
            throw new UnsupportedOperationException("不支持的类型,source:" + source + " target:" + target);
        }
        StringBuilder code = new StringBuilder();

        for (ClassProperty sourceProperty : sourceProperties.values()) {
            ClassProperty targetProperty = targetProperties.get(sourceProperty.getName());
            if (targetProperty == null) {
                if (targetIsExtendable && !sourceIsExtendable && !sourceIsMap) {
                    code.append("if(!ignore.contains(\"").append(sourceProperty.getName()).append("\")){\n\t");
                    if (!sourceProperty.isPrimitive()) {
                        code.append("if($$__source.").append(sourceProperty.getReadMethod()).append("!=null){\n");
                    }
                    code.append("\t\t((org.hswebframework.ezorm.core.Extendable)$$__target).setExtension(")
                        .append("\"").append(sourceProperty.name).append("\",")
                        .append("$$__source.").append(sourceProperty.getReadMethod())
                        .append(");");
                    if (!sourceProperty.isPrimitive()) {
                        code.append("\n\t}");
                    }
                    code.append("\n}\n");
                }
                continue;
            }
            code.append("if(!ignore.contains(\"").append(sourceProperty.getName()).append("\")){\n\t");
            if (!sourceProperty.isPrimitive()) {
                code.append("if($$__source.").append(sourceProperty.getReadMethod()).append("!=null){\n");
            }
            code.append(targetProperty.generateVar(targetProperty.getName())).append("=")
                .append(sourceProperty.generateGetter(target, targetProperty.getType()))
                .append(";\n");

            if (!targetProperty.isPrimitive()) {
                code.append("\tif(").append(sourceProperty.getName()).append("!=null){\n");
            }
            code.append("\t$$__target.")
                .append(targetProperty.generateSetter(targetProperty.getType(), sourceProperty.getName()))
                .append(";\n");
            if (!targetProperty.isPrimitive()) {
                code.append("\t}\n");
            }
            if (!sourceProperty.isPrimitive()) {
                code.append("\t}\n");
            }
            code.append("}\n");
        }
        return code.toString();
    }

    static Map<String, ClassProperty> createProperty(Class<?> type) {
        if (type.isRecord()) {
            return createRecordProperty(type);
        }

        List<String> fieldNames = Arrays
            .stream(type.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toList());

        return Stream.of(PROPERTY_UTILS.getPropertyDescriptors(type))
            .filter(property -> !property.getName().equals("class")
                && property.getReadMethod() != null
                && property.getWriteMethod() != null)
            .map(BeanClassProperty::new)
            .sorted(Comparator.comparing(property -> fieldNames.indexOf(property.name)))
            .collect(Collectors.toMap(ClassProperty::getName,
                                      Function.identity(),
                                      (k1, k2) -> k1,
                                      LinkedHashMap::new));
    }

    static Map<String, ClassProperty> createRecordProperty(Class<?> type) {
        return Arrays.stream(type.getRecordComponents())
            .map(RecordClassProperty::new)
            .collect(Collectors.toMap(ClassProperty::getName,
                                      Function.identity(),
                                      (k1, k2) -> k1,
                                      LinkedHashMap::new));
    }

    static Map<String, ClassProperty> createMapProperty(Map<String, ClassProperty> template) {
        return template.values()
            .stream()
            .map(classProperty -> new MapClassProperty(classProperty.name))
            .collect(Collectors.toMap(ClassProperty::getName,
                                      Function.identity(),
                                      (k1, k2) -> k1,
                                      LinkedHashMap::new));
    }

    abstract static class ClassProperty {
        @Getter
        protected String name;

        @Getter
        protected String readMethodName;

        @Getter
        protected String writeMethodName;

        @Getter
        protected BiFunction<Class<?>, Class<?>, String> getter;

        @Getter
        protected BiFunction<Class<?>, String, String> setter;

        @Getter
        protected Class<?> type;

        @Getter
        protected Class<?> beanType;

        public String getReadMethod() {
            return readMethodName + "()";
        }

        public String generateVar(String name) {
            return getTypeName().concat(" ").concat(name);
        }

        public String getTypeName() {
            return getTypeName(type);
        }

        public String getTypeName(Class<?> type) {
            String targetTypeName = type.getName();
            if (type.isArray()) {
                targetTypeName = type.getComponentType().getName() + "[]";
            }
            return targetTypeName;
        }

        public boolean isPrimitive() {
            return isPrimitive(getType());
        }

        public boolean isPrimitive(Class<?> type) {
            return type.isPrimitive();
        }

        public boolean isWrapper() {
            return isWrapper(getType());
        }

        public boolean isWrapper(Class<?> type) {
            return WRAPPER_CLASS_MAPPING.containsValue(type);
        }

        protected Class<?> getPrimitiveType(Class<?> type) {
            return WRAPPER_CLASS_MAPPING.entrySet().stream()
                .filter(entry -> entry.getValue() == type)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        }

        protected Class<?> getWrapperType() {
            return WRAPPER_CLASS_MAPPING.get(type);
        }

        protected String castWrapper(String getter) {
            return getWrapperType().getSimpleName().concat(".valueOf(").concat(getter).concat(")");
        }

        public BiFunction<Class<?>, Class<?>, String> createGetterFunction() {
            return (targetBeanType, targetType) -> {
                String getterCode = "$$__source." + getReadMethod();
                String generic = "org.hswebframework.web.bean.FastBeanCopierSupport.EMPTY_CLASS_ARRAY";
                Field field = ReflectionUtils.findField(targetBeanType, name);
                boolean hasGeneric = false;
                if (field != null) {
                    String[] arr = Arrays.stream(ResolvableType.forField(field, targetBeanType)
                            .getGenerics())
                        .map(ResolvableType::resolve)
                        .filter(Objects::nonNull)
                        .map(t -> t.getName().concat(".class"))
                        .toArray(String[]::new);
                    if (arr.length > 0) {
                        generic = "new Class[]{" + String.join(",", arr) + "}";
                        hasGeneric = true;
                    }
                }
                String sourceValue = "org.hswebframework.web.bean.FastBeanCopierSupport.unwrapEnumDictValue((Object)("
                    + (isPrimitive() ? castWrapper(getterCode) : getterCode)
                    + ")," + getTypeName(targetType) + ".class)";
                String convert = "converter.convert(" + sourceValue + ","
                    + getTypeName(targetType) + ".class," + generic + ")";
                StringBuilder convertCode = new StringBuilder();

                if (targetType != getType()) {
                    if (isPrimitive(targetType)) {
                        boolean sourceIsWrapper = isWrapper();
                        Class<?> targetWrapperClass = WRAPPER_CLASS_MAPPING.get(targetType);
                        Class<?> sourcePrimitive = getPrimitiveType(getType());
                        if (sourceIsWrapper) {
                            convertCode.append(getterCode)
                                .append(".")
                                .append(sourcePrimitive.getName())
                                .append("Value()");
                        } else {
                            convertCode.append("((").append(targetWrapperClass.getName())
                                .append(")")
                                .append(convert)
                                .append(").")
                                .append(targetType.getName())
                                .append("Value()");
                        }
                    } else if (isPrimitive()) {
                        boolean targetIsWrapper = isWrapper(targetType);
                        if (targetIsWrapper) {
                            convertCode.append(targetType.getName())
                                .append(".valueOf(")
                                .append(getterCode)
                                .append(")");
                        } else {
                            convertCode.append("(").append(targetType.getName())
                                .append(")(")
                                .append(convert)
                                .append(")");
                        }
                    } else {
                        convertCode.append("(").append(getTypeName(targetType))
                            .append(")(")
                            .append(convert)
                            .append(")");
                    }
                } else if (Cloneable.class.isAssignableFrom(targetType)) {
                    try {
                        convertCode.append("(")
                            .append(getTypeName())
                            .append(")")
                            .append(getterCode)
                            .append(".clone()");
                    } catch (Exception e) {
                        convertCode.append(getterCode);
                    }
                } else if ((Map.class.isAssignableFrom(targetType)
                    || Collection.class.isAssignableFrom(type)) && hasGeneric) {
                    convertCode.append("(").append(getTypeName()).append(")").append(convert);
                } else {
                    convertCode.append("(").append(getTypeName()).append(")").append(getterCode);
                }
                return convertCode.toString();
            };
        }

        public BiFunction<Class<?>, String, String> createSetterFunction(Function<String, String> settingNameSupplier) {
            return (sourceType, paramGetter) -> settingNameSupplier.apply(paramGetter);
        }

        public String generateGetter(Class<?> targetBeanType, Class<?> targetType) {
            return getGetter().apply(targetBeanType, targetType);
        }

        public String generateSetter(Class<?> targetType, String getter) {
            return getSetter().apply(targetType, getter);
        }
    }

    static class BeanClassProperty extends ClassProperty {
        BeanClassProperty(PropertyDescriptor descriptor) {
            type = descriptor.getPropertyType();
            readMethodName = descriptor.getReadMethod().getName();
            writeMethodName = descriptor.getWriteMethod().getName();
            getter = createGetterFunction();
            setter = createSetterFunction(paramGetter -> writeMethodName + "(" + paramGetter + ")");
            name = descriptor.getName();
            beanType = descriptor.getReadMethod().getDeclaringClass();
        }
    }

    static class RecordClassProperty extends ClassProperty {
        RecordClassProperty(RecordComponent component) {
            type = component.getType();
            readMethodName = component.getAccessor().getName();
            writeMethodName = null;
            getter = createGetterFunction();
            setter = createSetterFunction(paramGetter -> {
                throw new UnsupportedOperationException("Record property is read-only: " + component.getName());
            });
            name = component.getName();
            beanType = component.getDeclaringRecord();
        }
    }

    static class MapClassProperty extends ClassProperty {
        MapClassProperty(String name) {
            type = Object.class;
            this.name = name;
            this.readMethodName = "get";
            this.writeMethodName = "put";
            this.getter = createGetterFunction();
            this.setter = createSetterFunction(paramGetter -> "put(\"" + name + "\"," + paramGetter + ")");
            beanType = Map.class;
        }

        @Override
        public String getReadMethod() {
            return "get(\"" + name + "\")";
        }

        @Override
        public String getReadMethodName() {
            return "get(\"" + name + "\")";
        }
    }
}
