package org.hswebframework.web.service.form.simple.dict;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.OptionConverter;
import org.hswebframework.web.dict.EnumDict;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class EnumDictOptionConverter<T extends EnumDict> implements OptionConverter {

    protected Supplier<List<T>> allOptionSupplier;

    @Getter
    @Setter
    protected Function<Stream<String>, String> dictToText = stream -> stream.collect(Collectors.joining(","));

    @Setter
    @Getter
    protected Function<String, List<Object>> splitter = str -> Arrays.asList(str.split("[, ; ；]"));

    @Getter
    @Setter
    protected boolean writeObject;

    protected String fieldName;

    public EnumDictOptionConverter(Supplier<List<T>> supplier, String fieldName) {
        this.allOptionSupplier = supplier;
        this.fieldName = fieldName;
    }

    @Override
    public Object getOptions() {
        return allOptionSupplier.get();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Object converterData(Object o) {
        //什么也不做,EnumDictValueConverter会进行处理
        return o;
    }

    @Override
    @SuppressWarnings("all")
    public Object converterValue(Object o) {
        List<Object> values;
        if (o instanceof String) {
            values = splitter.apply((String) o);
        } else if (o instanceof Object[]) {
            values = Arrays.asList(((Object[]) o));
        } else if (o instanceof Collection) {
            values = new ArrayList<>(((Collection) o));
        } else {
            values = Collections.singletonList(o);
        }
        if (writeObject) {
            return allOptionSupplier.get()
                    .stream()
                    .filter(e -> e.eq(values))
                    .collect(Collectors.toSet());
        }
        return dictToText.apply(allOptionSupplier.get()
                .stream()
                .filter(e -> e.eq(values))
                .map(EnumDict::getText)
                .map(String::valueOf));
    }
}
