package org.hswebframework.web.service.form.simple.dict;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.ValueConverter;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class EnumDictValueConverter<T extends EnumDict> implements ValueConverter {

    protected Supplier<List<T>> allOptionSupplier;

    protected Function<Object, T> orElseGet = v -> {
        log.warn("选项[{}]在字典中不存在.全部选项:[{}]", v, allOptionSupplier.get());
        return null;
    };

    @Getter
    @Setter
    protected Function<Stream<String>, String> multiValueConvert = stream -> stream.collect(Collectors.joining(","));

    @Setter
    @Getter
    protected Function<String, List<Object>> splitter = str -> Arrays.asList(str.split("[, ; ；]"));

    public EnumDictValueConverter(Supplier<List<T>> allOptionSupplier) {
        this.allOptionSupplier = allOptionSupplier;
    }

    public EnumDictValueConverter(Supplier<List<T>> allOptionSupplier, Function<Object, T> orElseGet) {
        this.allOptionSupplier = allOptionSupplier;
        this.orElseGet = orElseGet;
    }

    @Setter
    @Getter
    private boolean multi = true;

    @Setter
    @Getter
    private boolean dataToMask = true;

    protected T find(Object value) {
        return allOptionSupplier.get()
                .stream()
                .filter(e -> e.eq(value))
                .findFirst()
                .orElseGet(() -> orElseGet.apply(value));

    }

    @Override
    @SuppressWarnings("all")
    public Object getData(Object value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        //多选
        if (multi) {
            List<Object> values;
            if (value instanceof String) {
                values = splitter.apply((String) value);
            } else if (value instanceof Object[]) {
                values = Arrays.asList(((Object[]) value));
            } else if (value instanceof Collection) {
                values = new ArrayList<>(((Collection) value));
            } else {
                values = Collections.singletonList(value);
            }
            //转为mask
            if (dataToMask) {
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
                return EnumDict.toMask(values.stream()
                        .map(this::find)
                        .filter(Objects::nonNull)
                        .toArray(EnumDict[]::new));
            }
            return multiValueConvert
                    .apply(values.stream()
                            .map(this::find)
                            .filter(Objects::nonNull)
                            .map(EnumDict::getValue)
                            .map(String::valueOf));
        }

        return Optional.ofNullable(this.find(value))
                .map(EnumDict::getValue)
                .orElse(value);
    }

    @Override
    public Object getValue(Object data) {
        if (multi) {
            if (dataToMask) {
                Long mask = null;
                if (org.hswebframework.utils.StringUtils.isNumber(data)) {
                    mask = org.hswebframework.utils.StringUtils.toLong(data);
                }
                if (mask != null) {
                    return multiValueConvert
                            .apply(EnumDict.getByMask(allOptionSupplier, mask)
                                    .stream()
                                    .map(EnumDict::getValue)
                                    .map(String::valueOf));
                }
            }
            List<Object> lst = splitter.apply(String.valueOf(data));
            return multiValueConvert
                    .apply(allOptionSupplier.get()
                            .stream()
                            .filter(e -> e.eq(lst))
                            .map(EnumDict::getValue)
                            .map(String::valueOf));
        }
        return data;
    }

}
