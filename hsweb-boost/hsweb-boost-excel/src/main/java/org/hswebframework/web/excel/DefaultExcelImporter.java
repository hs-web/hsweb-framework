package org.hswebframework.web.excel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.SneakyThrows;
import org.hswebframework.expands.office.excel.ExcelIO;
import org.hswebframework.web.bean.FastBeanCopier;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class DefaultExcelImporter implements ExcelImporter {

    protected static Map<Class, Map<Class, HeaderMapper>> headerMappings = new ConcurrentHashMap<>();

    @SuppressWarnings("all")
    protected Map<Class, HeaderMapper> createHeaderMapping(Class type) {
        //一些基本类型不做处理
        if (type == String.class
                || Number.class.isAssignableFrom(type)
                || ClassUtils.isPrimitiveWrapper(type)
                || type.isPrimitive()
                || type.isEnum()
                || type.isArray()
                || Date.class.isAssignableFrom(type)) {
            return Collections.emptyMap();
        }
        AtomicInteger index = new AtomicInteger(0);
        Map<Class, DefaultHeaderMapper> headerMapperMap = new HashMap<>();
        ReflectionUtils.doWithFields(type, field -> {
            Excel excel = field.getAnnotation(Excel.class);
            ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
            if ((excel == null && apiModelProperty == null) || (excel != null && excel.ignore())) {
                return;
            }
            String header = excel == null ? apiModelProperty.value() : excel.value();
            HeaderMapping mapping = new HeaderMapping();
            mapping.header = header;
            mapping.field = field.getName();
            mapping.index = excel == null || excel.sheetIndex() == -1 ? index.getAndAdd(1) : excel.sheetIndex();
            mapping.converter = createConvert(field.getType());
            if (null != excel) {
                mapping.enableImport = excel.enableImport();
                mapping.enableExport = excel.enableExport();
                mapping.children = () -> getHeaderMapper(field.getType(), excel.group());
                for (Class group : excel.group()) {
                    headerMapperMap.computeIfAbsent(group, DefaultHeaderMapper::new)
                            .mappings
                            .add(mapping);
                }
            } else {
                mapping.children = () -> getHeaderMapper(field.getType());
                headerMapperMap.computeIfAbsent(Void.class, DefaultHeaderMapper::new)
                        .mappings
                        .add(mapping);

            }
        });

        return (Map) headerMapperMap;
    }

    protected <T> ExcelCellConverter<T> createConvert(Class<T> type) {
        // TODO: 18-6-12
        return new ExcelCellConverter<T>() {
            @Override
            public T convertFromCell(Object from) {
                return type.cast(from);
            }

            @Override
            public Object convertToCell(T from) {
                return from;
            }
        };
    }

    @Getter
    class HeaderMapping implements Comparable<HeaderMapping> {
        private String field;

        private String header;

        private int index;

        private boolean enableExport = true;

        private boolean enableImport = true;

        private Supplier<HeaderMapper> children;

        private ExcelCellConverter converter;

        public HeaderMapping copy() {
            HeaderMapping mapping = new HeaderMapping();
            mapping.children = children;
            mapping.field = field;
            mapping.header = header;
            mapping.index = index;
            mapping.enableImport = enableImport;
            mapping.enableExport = enableExport;
            mapping.children = children;
            mapping.converter = converter;
            return mapping;
        }

        @Override
        public int compareTo(HeaderMapping o) {
            return Integer.compare(index, o.index);
        }
    }


    class DefaultHeaderMapper implements HeaderMapper {

        @Getter
        private Class group;

        public DefaultHeaderMapper(Class group) {
            this.group = group;
        }

        private Map<String, HeaderMapping> fastMapping = new HashMap<>();

        private final List<HeaderMapping> mappings = new ArrayList<HeaderMapping>() {
            private static final long serialVersionUID = 5995980497414973311L;

            @Override
            public boolean add(HeaderMapping o) {
                fastMapping.put(o.header, o);
                fastMapping.put(o.field, o);
                return super.add(o);
            }
        };


        @Override
        public Optional<HeaderMapping> getMapping(String key) {
            return Optional.ofNullable(fastMapping.computeIfAbsent(key, k -> {
                for (HeaderMapping mapping : mappings) {
                    String newKey = key;
                    if (newKey.startsWith(mapping.field)) {
                        newKey = newKey.substring(mapping.field.length());
                    } else if (newKey.startsWith(mapping.header)) {
                        newKey = newKey.substring(mapping.header.length());
                    } else {
                        continue;
                    }
                    HeaderMapper mapper = mapping.children.get();
                    if (null != mapper) {
                        HeaderMapping map = mapper.getMapping(newKey).orElse(null);
                        if (map != null) {
                            map = map.copy();
                            map.field = mapping.field.concat(".").concat(map.field);
                            return map;
                        }
                    }
                }
                return null;
            }));
        }
    }

    interface HeaderMapper {
        Optional<HeaderMapping> getMapping(String key);
    }

    protected HeaderMapper getHeaderMapper(Class type, Class... group) {
        Map<Class, HeaderMapper> mapperMap = headerMappings.computeIfAbsent(type, this::createHeaderMapping);

        if (group != null && group.length > 0) {
            return Arrays.stream(group)
                    .map(mapperMap::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        } else {
            return mapperMap.get(Void.class);
        }
    }

    @Override
    @SneakyThrows
    public <T> Result<T> doImport(InputStream inputStream, Class<T> type, Function<T, Error> validator, Class... group) {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger errorCounter = new AtomicInteger(0);
        List<T> data = new ArrayList<>();
        List<Error> errors = new ArrayList<>();
        HeaderMapper headerMapper = getHeaderMapper(type, group);
        if (headerMapper == null) {
            throw new UnsupportedOperationException("不支持导入此类型");
        }
        ExcelIO.read(inputStream, row -> {
            counter.getAndAdd(1);
            Map<String, Object> mapValue = row.getResult();

            Map<String, Object> newValue = new HashMap<>();

            for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                HeaderMapping mapping = headerMapper.getMapping(key).orElse(null);

                if (mapping == null || !mapping.enableImport) {
                    continue;
                }

                String field = mapping.field;
                //嵌套的字段
                if (field.contains(".")) {
                    String tmpField = field;
                    Map<String, Object> nestMapValue = newValue;
                    while (tmpField.contains(".")) {
                        String[] nestFields = tmpField.split("[.]", 2);
                        tmpField = nestFields[1];
                        Object nestValue = nestMapValue.get(nestFields[0]);
                        if (nestValue == null) {
                            nestMapValue.put(nestFields[0], nestMapValue = new HashMap<>());
                        } else {
                            if (nestValue instanceof Map) {
                                nestMapValue = ((Map) nestValue);
                            }
                        }
                    }
                    nestMapValue.put(tmpField, value);
                } else {
                    newValue.put(field, value);
                }
            }
            //创建实例并将map复制到实例中
            T instance = FastBeanCopier.getBeanFactory().newInstance(type);

            FastBeanCopier.copy(newValue, instance);

            data.add(instance);

            Error error = validator.apply(instance);
            if (null != error) {
                errorCounter.getAndAdd(1);
                error.setRowIndex(counter.get());
                error.setSheetIndex(row.getSheet());
                errors.add(error);
            }
        });
        return Result.<T>builder()
                .data(data)
                .errors(errors)
                .success(counter.get() - errorCounter.get())
                .total(counter.get())
                .error(errorCounter.get())
                .build();
    }
}
