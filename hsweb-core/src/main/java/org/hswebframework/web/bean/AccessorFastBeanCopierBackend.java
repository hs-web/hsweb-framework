package org.hswebframework.web.bean;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.Extendable;
import org.hswebframework.web.bean.accessor.ConverterPropertyTransfer;
import org.hswebframework.web.bean.accessor.PropertyAccessor;
import org.hswebframework.web.bean.accessor.PropertyReader;
import org.hswebframework.web.bean.accessor.PropertyTransfer;
import org.hswebframework.web.bean.accessor.PropertyWriter;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
abstract class AccessorFastBeanCopierBackend implements FastBeanCopierBackend {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap<>();
    /**
     * GenericKey 为瞬时组合 key，不能使用弱键缓存，否则 key 本身很容易被回收并导致热路径重复解析泛型。
     * 这里改用强缓存，通过 clearCache(ClassLoader) 显式回收动态 classloader 关联条目。
     */
    private static final Map<GenericKey, Class<?>[]> GENERIC_CACHE = new ConcurrentHashMap<>();

    static {
        PRIMITIVE_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPERS.put(short.class, Short.class);
        PRIMITIVE_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVE_WRAPPERS.put(float.class, Float.class);
        PRIMITIVE_WRAPPERS.put(double.class, Double.class);
        PRIMITIVE_WRAPPERS.put(char.class, Character.class);
        PRIMITIVE_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPERS.put(long.class, Long.class);
    }

    private final PropertyAccessor propertyAccessor;

    protected AccessorFastBeanCopierBackend(PropertyAccessor propertyAccessor) {
        this.propertyAccessor = Objects.requireNonNull(propertyAccessor, "propertyAccessor");
    }

    static void clearCache() {
        GENERIC_CACHE.clear();
    }

    static void clearCache(ClassLoader loader) {
        if (loader == null) {
            clearCache();
            return;
        }
        GENERIC_CACHE.keySet().removeIf(key -> key.involves(loader));
    }

    @Override
    public Copier createCopier(Class<?> source, Class<?> target) {
        try {
            Copier copier = new AccessorCopier(source, target, propertyAccessor);
            boolean targetIsExtendable = Extendable.class.isAssignableFrom(target);
            boolean sourceIsExtendable = Extendable.class.isAssignableFrom(source);
            boolean targetIsMap = Map.class.isAssignableFrom(target);
            boolean sourceIsMap = Map.class.isAssignableFrom(source);
            if (sourceIsExtendable && targetIsMap) {
                copier = new ExtendableToMapCopier(copier);
            } else if (sourceIsMap && targetIsExtendable) {
                copier = new MapToExtendableCopier(copier);
            } else if (sourceIsExtendable) {
                copier = new ExtendableToBeanCopier(copier);
            }
            return copier;
        } catch (Throwable e) {
            log.error("create bean copy {} => {} failed", source, target, e);
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    static final class AccessorCopier implements Copier {
        private final NamedPropertyTransfer[] transfers;
        private final ExtensionTransfer[] extensionTransfers;
        private final Map<String, NamedPropertyTransfer> sourceMapTransfers;
        private final boolean sourceIsMap;
        private final PropertyTransfer[] fastAccessorTransfers;
        private final boolean fastAccessorMode;

        AccessorCopier(Class<?> source, Class<?> target, PropertyAccessor propertyAccessor) {
            Map<String, FastBeanCopierPropertySupport.ClassProperty> sourceProperties = null;
            Map<String, FastBeanCopierPropertySupport.ClassProperty> targetProperties = null;

            boolean targetIsExtendable = Extendable.class.isAssignableFrom(target);
            boolean sourceIsExtendable = Extendable.class.isAssignableFrom(source);
            boolean targetIsMap = Map.class.isAssignableFrom(target);
            boolean sourceIsMap = Map.class.isAssignableFrom(source);
            if (sourceIsMap) {
                if (!targetIsMap) {
                    targetProperties = FastBeanCopierPropertySupport.createProperty(target);
                    sourceProperties = FastBeanCopierPropertySupport.createMapProperty(targetProperties);
                }
            } else if (targetIsMap) {
                sourceProperties = FastBeanCopierPropertySupport.createProperty(source);
                targetProperties = FastBeanCopierPropertySupport.createMapProperty(sourceProperties);
            } else {
                targetProperties = FastBeanCopierPropertySupport.createProperty(target);
                sourceProperties = FastBeanCopierPropertySupport.createProperty(source);
            }
            if (sourceProperties == null || targetProperties == null) {
                throw new UnsupportedOperationException("不支持的类型,source:" + source + " target:" + target);
            }

            List<NamedPropertyTransfer> mappings = new ArrayList<>(sourceProperties.size());
            List<ExtensionTransfer> extensionMappings = new ArrayList<>();
            List<PropertyTransfer> fastTransfers = new ArrayList<>(sourceProperties.size());
            boolean accessorOnly = !sourceIsMap && !targetIsMap && !targetIsExtendable;
            for (FastBeanCopierPropertySupport.ClassProperty sourceProperty : sourceProperties.values()) {
                FastBeanCopierPropertySupport.ClassProperty targetProperty = targetProperties.get(sourceProperty.getName());
                if (targetProperty == null) {
                    if (targetIsExtendable && !sourceIsExtendable && !sourceIsMap) {
                        extensionMappings.add(new ExtensionTransfer(sourceProperty, propertyAccessor));
                    }
                    continue;
                }
                NamedPropertyTransfer transfer = NamedPropertyTransfer.of(target, sourceProperty, targetProperty, propertyAccessor);
                mappings.add(transfer);
                if (accessorOnly && transfer instanceof AccessorPropertyTransfer) {
                    fastTransfers.add(((AccessorPropertyTransfer) transfer).transfer);
                } else {
                    accessorOnly = false;
                }
            }
            this.transfers = mappings.toArray(new NamedPropertyTransfer[0]);
            this.extensionTransfers = extensionMappings.toArray(new ExtensionTransfer[0]);
            this.sourceIsMap = sourceIsMap;
            this.fastAccessorMode = accessorOnly && !fastTransfers.isEmpty() && fastTransfers.size() == mappings.size();
            this.fastAccessorTransfers = fastAccessorMode
                ? fastTransfers.toArray(new PropertyTransfer[0])
                : null;
            if (sourceIsMap) {
                Map<String, NamedPropertyTransfer> transferIndex = new HashMap<>(mappings.size());
                for (NamedPropertyTransfer transfer : mappings) {
                    transferIndex.put(transfer.name, transfer);
                }
                this.sourceMapTransfers = transferIndex;
            } else {
                this.sourceMapTransfers = Collections.emptyMap();
            }
        }

        @Override
        public void copy(Object source, Object target, Set<String> ignore, Converter converter) {
            try {
                if (sourceIsMap) {
                    if (ignore.isEmpty()) {
                        copyMapWithoutIgnore((Map<?, ?>) source, target, converter);
                    } else {
                        copyMapWithIgnore((Map<?, ?>) source, target, ignore, converter);
                    }
                } else if (ignore.isEmpty()) {
                    copyWithoutIgnore(source, target, converter);
                } else {
                    copyWithIgnore(source, target, ignore, converter);
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new UnsupportedOperationException(e.getMessage(), e);
            }
        }

        private void copyWithoutIgnore(Object source, Object target, Converter converter) throws Throwable {
            if (fastAccessorMode) {
                for (PropertyTransfer transfer : fastAccessorTransfers) {
                    transfer.transfer(source, target);
                }
                return;
            }
            for (NamedPropertyTransfer transfer : transfers) {
                transfer.transfer(source, target, converter);
            }
            if (extensionTransfers.length != 0) {
                Extendable extendable = (Extendable) target;
                for (ExtensionTransfer transfer : extensionTransfers) {
                    transfer.transfer(source, extendable);
                }
            }
        }

        private void copyWithIgnore(Object source, Object target, Set<String> ignore, Converter converter) throws Throwable {
            for (NamedPropertyTransfer transfer : transfers) {
                transfer.transfer(source, target, ignore, converter);
            }
            if (extensionTransfers.length != 0) {
                Extendable extendable = (Extendable) target;
                for (ExtensionTransfer transfer : extensionTransfers) {
                    transfer.transfer(source, extendable, ignore);
                }
            }
        }

        private void copyMapWithoutIgnore(Map<?, ?> source, Object target, Converter converter) throws Throwable {
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                NamedPropertyTransfer transfer = sourceMapTransfers.get(String.valueOf(entry.getKey()));
                if (transfer != null) {
                    transfer.transferValue(entry.getValue(), target, converter);
                }
            }
        }

        private void copyMapWithIgnore(Map<?, ?> source, Object target, Set<String> ignore, Converter converter) throws Throwable {
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                String name = String.valueOf(entry.getKey());
                if (ignore.contains(name)) {
                    continue;
                }
                NamedPropertyTransfer transfer = sourceMapTransfers.get(name);
                if (transfer != null) {
                    transfer.transferValue(entry.getValue(), target, converter);
                }
            }
        }
    }

    abstract static class NamedPropertyTransfer {
        private final String name;

        NamedPropertyTransfer(String name) {
            this.name = name;
        }

        final void transfer(Object source, Object target, Set<String> ignore, Converter converter) throws Throwable {
            if (!ignore.contains(name)) {
                transfer(source, target, converter);
            }
        }

        abstract void transfer(Object source, Object target, Converter converter) throws Throwable;

        void transferValue(Object value, Object target, Converter converter) throws Throwable {
            throw new UnsupportedOperationException("transferValue is only supported for map source transfers");
        }

        static NamedPropertyTransfer of(Class<?> target,
                                        FastBeanCopierPropertySupport.ClassProperty sourceProperty,
                                        FastBeanCopierPropertySupport.ClassProperty targetProperty,
                                        PropertyAccessor propertyAccessor) {
            Class<?> sourceType = sourceProperty.getType();
            Class<?> targetType = targetProperty.getType();
            boolean sourcePrimitive = sourceProperty.isPrimitive();
            boolean targetPrimitive = targetProperty.isPrimitive();
            Class<?>[] genericTypes = resolveGenericTypes(target, targetProperty);

            if (Map.class.isAssignableFrom(sourceProperty.getBeanType())) {
                ValueWriter writer = createWriter(targetProperty, propertyAccessor);
                if (targetType == Object.class) {
                    return new MapDirectPropertyTransfer(sourceProperty.getName(), writer, targetPrimitive);
                }
                if (requiresGenericConversion(targetType, genericTypes)) {
                    return new MapConvertingPropertyTransfer(sourceProperty.getName(),
                                                             writer,
                                                             targetPrimitive,
                                                             targetType,
                                                             genericTypes,
                                                             isNumberType(targetType),
                                                             false);
                }
                return new MapConvertingPropertyTransfer(sourceProperty.getName(),
                                                         writer,
                                                         targetPrimitive,
                                                         targetType,
                                                         genericTypes,
                                                         isNumberType(targetType),
                                                         true);
            }

            PropertyTransfer accessorTransfer = createAccessorTransfer(sourceProperty,
                                                                      targetProperty,
                                                                      propertyAccessor,
                                                                      sourceType,
                                                                      targetType,
                                                                      sourcePrimitive,
                                                                      genericTypes);
            if (accessorTransfer != null) {
                return new AccessorPropertyTransfer(sourceProperty.getName(), accessorTransfer);
            }

            boolean allowDirectAssignment = sourceType != targetType;
            ConverterPropertyTransfer accessorConverterTransfer = createAccessorConverterTransfer(sourceProperty,
                                                                                                 targetProperty,
                                                                                                 propertyAccessor,
                                                                                                 sourceType,
                                                                                                 targetType,
                                                                                                 sourcePrimitive,
                                                                                                 genericTypes,
                                                                                                 allowDirectAssignment);
            if (accessorConverterTransfer != null) {
                return new AccessorConverterTransfer(sourceProperty.getName(), accessorConverterTransfer);
            }

            ValueReader reader = createReader(sourceProperty, propertyAccessor);
            ValueWriter writer = createWriter(targetProperty, propertyAccessor);
            if (sourceType == targetType) {
                if (Cloneable.class.isAssignableFrom(targetType)) {
                    return new CloneablePropertyTransfer(sourceProperty.getName(),
                                                         reader,
                                                         writer,
                                                         sourcePrimitive,
                                                         targetType,
                                                         genericTypes);
                }
                if (requiresGenericConversion(targetType, genericTypes)) {
                    return new ConvertingPropertyTransfer(sourceProperty.getName(),
                                                          reader,
                                                          writer,
                                                          sourcePrimitive,
                                                          targetPrimitive,
                                                          targetType,
                                                          genericTypes,
                                                          false,
                                                          false);
                }
                return new DirectPropertyTransfer(sourceProperty.getName(), reader, writer, sourcePrimitive);
            }

            if (targetType == Object.class || isStaticallyDirectAssignable(sourceType, targetType)) {
                return new DirectPropertyTransfer(sourceProperty.getName(), reader, writer, sourcePrimitive);
            }

            return new ConvertingPropertyTransfer(sourceProperty.getName(),
                                                  reader,
                                                  writer,
                                                  sourcePrimitive,
                                                  targetPrimitive,
                                                  targetType,
                                                  genericTypes,
                                                  isNumberType(targetType),
                                                  true);
        }

        static ValueReader createReader(FastBeanCopierPropertySupport.ClassProperty property,
                                        PropertyAccessor propertyAccessor) {
            if (Map.class.isAssignableFrom(property.getBeanType())) {
                return source -> ((Map<?, ?>) source).get(property.getName());
            }
            PropertyReader reader = propertyAccessor.createReader(property.getBeanType(), property.getName());
            return reader::apply;
        }

        @SuppressWarnings("unchecked")
        static ValueWriter createWriter(FastBeanCopierPropertySupport.ClassProperty property,
                                        PropertyAccessor propertyAccessor) {
            if (Map.class.isAssignableFrom(property.getBeanType())) {
                return (target, value) -> ((Map<String, Object>) target).put(property.getName(), value);
            }
            PropertyWriter writer = propertyAccessor.createWriter(property.getBeanType(), property.getName(), null);
            return writer::accept;
        }

        private static Class<?>[] resolveGenericTypes(Class<?> target,
                                                      FastBeanCopierPropertySupport.ClassProperty property) {
            if (Map.class.isAssignableFrom(property.getBeanType())) {
                return FastBeanCopierSupport.EMPTY_CLASS_ARRAY;
            }
            return GENERIC_CACHE.computeIfAbsent(new GenericKey(target, property.getBeanType(), property.getName()), key -> {
                Field field = ReflectionUtils.findField(key.targetType, key.propertyName);
                if (field == null) {
                    field = ReflectionUtils.findField(key.beanType, key.propertyName);
                }
                if (field == null) {
                    return FastBeanCopierSupport.EMPTY_CLASS_ARRAY;
                }
                Class<?>[] arr = Arrays.stream(ResolvableType.forField(field, target).getGenerics())
                    .map(ResolvableType::resolve)
                    .filter(Objects::nonNull)
                    .toArray(Class[]::new);
                return arr.length == 0 ? FastBeanCopierSupport.EMPTY_CLASS_ARRAY : arr;
            });
        }

        private static PropertyTransfer createAccessorTransfer(FastBeanCopierPropertySupport.ClassProperty sourceProperty,
                                                               FastBeanCopierPropertySupport.ClassProperty targetProperty,
                                                               PropertyAccessor propertyAccessor,
                                                               Class<?> sourceType,
                                                               Class<?> targetType,
                                                               boolean sourcePrimitive,
                                                               Class<?>[] genericTypes) {
            if (Map.class.isAssignableFrom(sourceProperty.getBeanType()) || Map.class.isAssignableFrom(targetProperty.getBeanType())) {
                return null;
            }
            if (sourceType == targetType) {
                if (Cloneable.class.isAssignableFrom(targetType) || requiresGenericConversion(targetType, genericTypes)) {
                    return null;
                }
            } else if (targetType != Object.class && !isStaticallyDirectAssignable(sourceType, targetType)) {
                return null;
            }
            return propertyAccessor.createTransfer(sourceProperty.getBeanType(),
                                                   sourceProperty.getName(),
                                                   sourcePrimitive,
                                                   targetProperty.getBeanType(),
                                                   targetProperty.getName());
        }

        private static ConverterPropertyTransfer createAccessorConverterTransfer(FastBeanCopierPropertySupport.ClassProperty sourceProperty,
                                                                                FastBeanCopierPropertySupport.ClassProperty targetProperty,
                                                                                PropertyAccessor propertyAccessor,
                                                                                Class<?> sourceType,
                                                                                Class<?> targetType,
                                                                                boolean sourcePrimitive,
                                                                                Class<?>[] genericTypes,
                                                                                boolean allowDirectAssignment) {
            if (Map.class.isAssignableFrom(sourceProperty.getBeanType()) || Map.class.isAssignableFrom(targetProperty.getBeanType())) {
                return null;
            }
            if (sourceType == targetType) {
                if (!requiresGenericConversion(targetType, genericTypes) || Cloneable.class.isAssignableFrom(targetType)) {
                    return null;
                }
            } else if (targetType == Object.class || isStaticallyDirectAssignable(sourceType, targetType)) {
                return null;
            }
            return propertyAccessor.createConverterTransfer(sourceProperty.getBeanType(),
                                                           sourceProperty.getName(),
                                                           sourcePrimitive,
                                                           targetProperty.getBeanType(),
                                                           targetProperty.getName(),
                                                           allowDirectAssignment);
        }
    }

    static final class AccessorPropertyTransfer extends NamedPropertyTransfer {
        private final PropertyTransfer transfer;

        AccessorPropertyTransfer(String name, PropertyTransfer transfer) {
            super(name);
            this.transfer = transfer;
        }

        @Override
        void transfer(Object source, Object target, Converter converter) {
            transfer.transfer(source, target);
        }
    }

    static final class AccessorConverterTransfer extends NamedPropertyTransfer {
        private final ConverterPropertyTransfer transfer;

        AccessorConverterTransfer(String name, ConverterPropertyTransfer transfer) {
            super(name);
            this.transfer = transfer;
        }

        @Override
        void transfer(Object source, Object target, Converter converter) {
            transfer.transfer(source, target, converter);
        }
    }

    abstract static class ReaderWriterPropertyTransfer extends NamedPropertyTransfer {
        protected final ValueReader reader;
        protected final ValueWriter writer;
        protected final boolean sourcePrimitive;

        ReaderWriterPropertyTransfer(String name,
                                     ValueReader reader,
                                     ValueWriter writer,
                                     boolean sourcePrimitive) {
            super(name);
            this.reader = reader;
            this.writer = writer;
            this.sourcePrimitive = sourcePrimitive;
        }

        protected final Object readValue(Object source) throws Throwable {
            Object value = reader.read(source);
            if (value == null && !sourcePrimitive) {
                return null;
            }
            return value;
        }
    }

    static final class DirectPropertyTransfer extends ReaderWriterPropertyTransfer {

        DirectPropertyTransfer(String name,
                               ValueReader reader,
                               ValueWriter writer,
                               boolean sourcePrimitive) {
            super(name, reader, writer, sourcePrimitive);
        }

        @Override
        void transfer(Object source, Object target, Converter converter) throws Throwable {
            Object value = readValue(source);
            if (value == null && !sourcePrimitive) {
                return;
            }
            writer.write(target, value);
        }
    }

    static final class MapDirectPropertyTransfer extends NamedPropertyTransfer {
        private final ValueWriter writer;
        private final boolean targetPrimitive;

        MapDirectPropertyTransfer(String name, ValueWriter writer, boolean targetPrimitive) {
            super(name);
            this.writer = writer;
            this.targetPrimitive = targetPrimitive;
        }

        @Override
        void transfer(Object source, Object target, Converter converter) {
            throw new UnsupportedOperationException();
        }

        @Override
        void transferValue(Object value, Object target, Converter converter) throws Throwable {
            if (value == null && !targetPrimitive) {
                return;
            }
            writer.write(target, value);
        }
    }

    static final class MapConvertingPropertyTransfer extends NamedPropertyTransfer {
        private final ValueWriter writer;
        private final boolean targetPrimitive;
        private final Class<?> targetType;
        private final Class<?>[] genericTypes;
        private final boolean unwrapEnumDictNumber;
        private final boolean allowDirectAssignable;
        private final boolean beanLikeTarget;

        MapConvertingPropertyTransfer(String name,
                                      ValueWriter writer,
                                      boolean targetPrimitive,
                                      Class<?> targetType,
                                      Class<?>[] genericTypes,
                                      boolean unwrapEnumDictNumber,
                                      boolean allowDirectAssignable) {
            super(name);
            this.writer = writer;
            this.targetPrimitive = targetPrimitive;
            this.targetType = targetType;
            this.genericTypes = genericTypes;
            this.unwrapEnumDictNumber = unwrapEnumDictNumber;
            this.allowDirectAssignable = allowDirectAssignable;
            this.beanLikeTarget = isBeanLikeTarget(targetType);
        }

        @Override
        void transfer(Object source, Object target, Converter converter) {
            throw new UnsupportedOperationException();
        }

        @Override
        @SuppressWarnings("unchecked")
        void transferValue(Object value, Object target, Converter converter) throws Throwable {
            if (value == null && !targetPrimitive) {
                return;
            }
            if (unwrapEnumDictNumber && value instanceof EnumDict) {
                Object enumValue = ((EnumDict<?>) value).getValue();
                if (enumValue != null) {
                    value = enumValue;
                }
            }
            if (allowDirectAssignable && isDirectAssignable(targetType, value)) {
                writer.write(target, value);
                return;
            }
            if (beanLikeTarget && value instanceof Map) {
                Object nested = FastBeanCopierSupport.copy(value,
                                                           FastBeanCopierSupport.getBeanFactory().newInstance(targetType),
                                                           converter,
                                                           Collections.emptySet());
                writer.write(target, nested);
                return;
            }
            Object converted = converter.convert(value, (Class) targetType, genericTypes);
            if (converted == null && !targetPrimitive) {
                return;
            }
            writer.write(target, converted);
        }
    }

    static final class CloneablePropertyTransfer extends ReaderWriterPropertyTransfer {
        private final Class<?> targetType;
        private final Class<?>[] genericTypes;

        CloneablePropertyTransfer(String name,
                                  ValueReader reader,
                                  ValueWriter writer,
                                  boolean sourcePrimitive,
                                  Class<?> targetType,
                                  Class<?>[] genericTypes) {
            super(name, reader, writer, sourcePrimitive);
            this.targetType = targetType;
            this.genericTypes = genericTypes;
        }

        @Override
        @SuppressWarnings("unchecked")
        void transfer(Object source, Object target, Converter converter) throws Throwable {
            Object value = readValue(source);
            if (value == null && !sourcePrimitive) {
                return;
            }
            Object cloned = tryClone(value);
            if (cloned != null) {
                writer.write(target, cloned);
                return;
            }
            if (requiresGenericConversion(targetType, genericTypes)) {
                Object converted = converter.convert(value, (Class) targetType, genericTypes);
                if (converted != null) {
                    writer.write(target, converted);
                }
                return;
            }
            writer.write(target, value);
        }
    }

    static final class ConvertingPropertyTransfer extends ReaderWriterPropertyTransfer {
        private final boolean targetPrimitive;
        private final Class<?> targetType;
        private final Class<?>[] genericTypes;
        private final boolean unwrapEnumDictNumber;
        private final boolean allowDirectAssignable;
        private final boolean beanLikeTarget;

        ConvertingPropertyTransfer(String name,
                                   ValueReader reader,
                                   ValueWriter writer,
                                   boolean sourcePrimitive,
                                   boolean targetPrimitive,
                                   Class<?> targetType,
                                   Class<?>[] genericTypes,
                                   boolean unwrapEnumDictNumber,
                                   boolean allowDirectAssignable) {
            super(name, reader, writer, sourcePrimitive);
            this.targetPrimitive = targetPrimitive;
            this.targetType = targetType;
            this.genericTypes = genericTypes;
            this.unwrapEnumDictNumber = unwrapEnumDictNumber;
            this.allowDirectAssignable = allowDirectAssignable;
            this.beanLikeTarget = isBeanLikeTarget(targetType);
        }

        @Override
        @SuppressWarnings("unchecked")
        void transfer(Object source, Object target, Converter converter) throws Throwable {
            Object value = readValue(source);
            if (value == null && !sourcePrimitive) {
                return;
            }
            if (unwrapEnumDictNumber && value instanceof EnumDict) {
                Object enumValue = ((EnumDict<?>) value).getValue();
                if (enumValue != null) {
                    value = enumValue;
                }
            }
            if (allowDirectAssignable && isDirectAssignable(targetType, value)) {
                writer.write(target, value);
                return;
            }
            if (beanLikeTarget && value instanceof Map) {
                Object nested = FastBeanCopierSupport.copy(value,
                                                           FastBeanCopierSupport.getBeanFactory().newInstance(targetType),
                                                           converter,
                                                           Collections.emptySet());
                writer.write(target, nested);
                return;
            }
            Object converted = converter.convert(value, (Class) targetType, genericTypes);
            if (converted == null && !targetPrimitive) {
                return;
            }
            writer.write(target, converted);
        }
    }

    static final class ExtensionTransfer {
        private final String name;
        private final boolean primitive;
        private final ValueReader reader;

        ExtensionTransfer(FastBeanCopierPropertySupport.ClassProperty property,
                          PropertyAccessor propertyAccessor) {
            this.name = property.getName();
            this.primitive = property.isPrimitive();
            this.reader = NamedPropertyTransfer.createReader(property, propertyAccessor);
        }

        void transfer(Object source, Extendable target) throws Throwable {
            Object value = reader.read(source);
            if (value == null && !primitive) {
                return;
            }
            target.setExtension(name, value);
        }

        void transfer(Object source, Extendable target, Set<String> ignore) throws Throwable {
            if (!ignore.contains(name)) {
                transfer(source, target);
            }
        }
    }

    private static boolean requiresGenericConversion(Class<?> targetType, Class<?>[] genericTypes) {
        return genericTypes.length > 0
            && (Map.class.isAssignableFrom(targetType) || Collection.class.isAssignableFrom(targetType));
    }

    private static boolean isStaticallyDirectAssignable(Class<?> sourceType, Class<?> targetType) {
        return toWrapperType(targetType).isAssignableFrom(toWrapperType(sourceType));
    }

    private static Class<?> toWrapperType(Class<?> type) {
        return type.isPrimitive() ? PRIMITIVE_WRAPPERS.getOrDefault(type, type) : type;
    }

    private static boolean isDirectAssignable(Class<?> targetType, Object value) {
        if (value == null) {
            return false;
        }
        if (targetType.isInstance(value)) {
            return true;
        }
        if (targetType.isPrimitive()) {
            Class<?> wrapper = PRIMITIVE_WRAPPERS.get(targetType);
            return wrapper != null && wrapper.isInstance(value);
        }
        return false;
    }

    private static boolean isNumberType(Class<?> targetType) {
        if (Number.class.isAssignableFrom(targetType)) {
            return true;
        }
        return targetType.isPrimitive()
            && targetType != boolean.class
            && targetType != char.class;
    }

    private static boolean isBeanLikeTarget(Class<?> targetType) {
        return targetType != Object.class
            && targetType != String.class
            && targetType != CharSequence.class
            && targetType != Date.class
            && targetType != Boolean.class
            && targetType != Character.class
            && targetType != boolean.class
            && targetType != char.class
            && !targetType.isEnum()
            && !targetType.isArray()
            && !Collection.class.isAssignableFrom(targetType)
            && !Map.class.isAssignableFrom(targetType)
            && !isNumberType(targetType);
    }

    private static Object tryClone(Object value) {
        try {
            if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                Object clone = Array.newInstance(value.getClass().getComponentType(), length);
                System.arraycopy(value, 0, clone, 0, length);
                return clone;
            }
            Method clone = value.getClass().getMethod("clone");
            ReflectionUtils.makeAccessible(clone);
            return clone.invoke(value);
        } catch (Throwable ignore) {
            return null;
        }
    }

    interface ValueReader {
        Object read(Object source) throws Throwable;
    }

    interface ValueWriter {
        void write(Object target, Object value) throws Throwable;
    }

    private static final class GenericKey {
        private final Class<?> targetType;
        private final Class<?> beanType;
        private final String propertyName;

        private GenericKey(Class<?> targetType, Class<?> beanType, String propertyName) {
            this.targetType = targetType;
            this.beanType = beanType;
            this.propertyName = propertyName;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof GenericKey)) {
                return false;
            }
            GenericKey that = (GenericKey) obj;
            return targetType == that.targetType
                && beanType == that.beanType
                && Objects.equals(propertyName, that.propertyName);
        }

        @Override
        public int hashCode() {
            int result = System.identityHashCode(targetType);
            result = 31 * result + System.identityHashCode(beanType);
            result = 31 * result + propertyName.hashCode();
            return result;
        }

        private boolean involves(ClassLoader loader) {
            return FastBeanCopierSupport.isClassLoaderMatch(targetType, loader)
                || FastBeanCopierSupport.isClassLoaderMatch(beanType, loader);
        }
    }
}
