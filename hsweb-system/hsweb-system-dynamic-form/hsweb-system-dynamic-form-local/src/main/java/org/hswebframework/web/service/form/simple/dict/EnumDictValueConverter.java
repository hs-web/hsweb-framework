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

@Slf4j
public class EnumDictValueConverter<T extends EnumDict> implements ValueConverter {

    protected Supplier<List<T>> allOptionSupplier;

    protected Function<Object, T> orElseGet = v -> {
        log.warn("选项[{}]在字典中不存在.全部选项:[{}]", v, allOptionSupplier.get());
        return null;
    };

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
    public Object getData(Object o) {
        if (StringUtils.isEmpty(o)) {
            return o;
        }
        //多选
        if (multi) {
            List<Object> values;
            if (o instanceof String) {
                values = Arrays.asList(((String) o).split("[, ; ；]"));
            } else if (o instanceof Object[]) {
                values = Arrays.asList(((Object[]) o));
            } else if (o instanceof Collection) {
                values = new ArrayList<>(((Collection) o));
            } else {
                values = Collections.singletonList(o);
            }
            //转为mask
            if (dataToMask) {
                if (o instanceof Number) {
                    return ((Number) o).longValue();
                }
                return EnumDict.toMask(values.stream()
                        .map(this::find)
                        .filter(Objects::nonNull)
                        .toArray(EnumDict[]::new));
            }
            //以逗号分隔
            return values.stream()
                    .map(this::find)
                    .filter(Objects::nonNull)
                    .map(EnumDict::getValue)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

        return Optional.ofNullable(this.find(o))
                .map(EnumDict::getValue)
                .orElse(o);
    }

    @Override
    public Object getValue(Object o) {
        if (multi) {
            if (dataToMask) {
                Long mask = null;
                if (org.hswebframework.utils.StringUtils.isNumber(o)) {
                    mask = org.hswebframework.utils.StringUtils.toLong(o);
                }
                if (mask != null) {
                    return EnumDict.getByMask(allOptionSupplier, mask)
                            .stream()
                            .map(EnumDict::getValue)
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));
                }
            }
            return allOptionSupplier.get()
                    .stream()
                    .filter(e -> e.eq(o))
                    .map(EnumDict::getValue)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        return o;
    }

}
