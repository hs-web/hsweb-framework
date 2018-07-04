package org.hswebframework.web.service.form.simple.convert;

import org.hswebframework.ezorm.core.ValueConverter;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class SmartValueConverter implements ValueConverter {

    List<ValueConverter> converters;

    public SmartValueConverter(List<ValueConverter> converters) {
        this.converters = converters;
    }

    @Override
    public Object getData(Object value) {
        for (ValueConverter converter : converters) {
            value = converter.getData(value);
        }
        return value;
    }

    @Override
    public Object getValue(Object data) {
        for (ValueConverter converter : converters) {
            data = converter.getData(data);
        }
        return data;
    }
}
