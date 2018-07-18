package org.hswebframework.web.excel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.expands.office.excel.ExcelIO;
import org.hswebframework.web.ApplicationContextHolder;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.dict.ItemDefine;
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
@Slf4j
@SuppressWarnings("all")
public class DefaultExcelImporter implements ExcelImporter {

    protected static Map<Class, Map<Class, HeaderMapper>> headerMappings = new ConcurrentHashMap<>();

    protected static ExcelCellConverter DEFAULT_CONVERTER = new ExcelCellConverter() {
        @Override
        public Object convertFromCell(Object from) {
            return from;
        }

        @Override
        public Object convertToCell(Object from) {
            if (from instanceof EnumDict) {
                return ((EnumDict) from).getText();
            }
            return from;
        }
    };

    protected ExcelCellConverter defaultConvert = DEFAULT_CONVERTER;

    protected Map<Class, HeaderMapper> createHeaderMapping(Class type) {
        //一些基本类型不做处理
        if (type == String.class
                || Number.class.isAssignableFrom(type)
                || ClassUtils.isPrimitiveWrapper(type)
                || type.isPrimitive()
                || type.isEnum()
                || type.isArray()
                || Date.class.isAssignableFrom(type)) {
            return new java.util.HashMap<>();
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
            mapping.index = excel == null || excel.exportOrder() == -1 ? index.getAndAdd(1) : excel.exportOrder();
            if (null != excel) {
                mapping.enableImport = excel.enableImport();
                mapping.enableExport = excel.enableExport();
                mapping.children = () -> getHeaderMapper(field.getType(), excel.group());
                for (Class group : excel.group()) {
                    headerMapperMap.computeIfAbsent(group, DefaultHeaderMapper::new)
                            .mappings
                            .add(mapping);
                }
                mapping.converter = createConvert(excel.converter(), field.getType());
            } else {
                mapping.converter = createConvert(ExcelCellConverter.class, field.getType());
                mapping.children = () -> getHeaderMapper(field.getType());
                headerMapperMap.computeIfAbsent(Void.class, DefaultHeaderMapper::new)
                        .mappings
                        .add(mapping);

            }
        });

        return (Map) headerMapperMap;
    }

    @SneakyThrows
    protected <T> ExcelCellConverter<T> createConvert(Class<ExcelCellConverter> converterClass, Class<T> type) {
        if (converterClass != ExcelCellConverter.class) {
            try {
                return ApplicationContextHolder.get().getBean(converterClass);
            } catch (Exception e) {
                log.warn("can not get bean ({}) from spring context.", converterClass, e);
                return converterClass.newInstance();
            }
        }
        return defaultConvert;
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
                //尝试获取嵌套的属性
                for (HeaderMapping mapping : mappings) {
                    String newKey = key;
                    //字段嵌套
                    if (newKey.startsWith(mapping.field)) {
                        newKey = newKey.substring(mapping.field.length());
                    }
                    //表头嵌套
                    else if (newKey.startsWith(mapping.header)) {
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
                            map.header = mapping.header.concat(map.header);
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
    public <T> Result<T> doImport(InputStream inputStream, Class<T> type, Function<T, Error> afterParsed, Class... group) {
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
                HeaderMapping mapping = headerMapper.getMapping(key).orElse(null);

                if (mapping == null || !mapping.enableImport) {
                    continue;
                }
                Object value = mapping.getConverter().convertFromCell(entry.getValue());

                String field = mapping.getField();
                //嵌套的字段
                if (field.contains(".")) {
                    String tmpField = field;
                    Map<String, Object> nestMapValue = newValue;

                    while (tmpField.contains(".")) {

                        // nest.obj.name => [nest,obj.name]
                        String[] nestFields = tmpField.split("[.]", 2);

                        //nest
                        String nestField = nestFields[0];

                        //obj.name
                        tmpField = nestFields[1];

                        Object nestValue = nestMapValue.get(nestField);
                        //构造嵌套对象为map
                        if (nestValue == null) {
                            nestMapValue.put(nestField, nestMapValue = new HashMap<>());
                        } else {
                            if (nestValue instanceof Map) {
                                nestMapValue = ((Map) nestValue);
                            } else {
                                //这里几乎不可能进入...
                                nestMapValue.put(nestField, nestMapValue = FastBeanCopier.copy(nestValue, new HashMap<>()));
                            }
                        }
                    }
                    //最后nestMapValue就为最里层嵌套的对象了
                    nestMapValue.put(tmpField, value);
                } else {
                    newValue.put(field, value);
                }
            }
            //创建实例并将map复制到实例中
            T instance = FastBeanCopier.getBeanFactory().newInstance(type);

            FastBeanCopier.copy(newValue, instance);

            data.add(instance);

            Error error = afterParsed.apply(instance);
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
