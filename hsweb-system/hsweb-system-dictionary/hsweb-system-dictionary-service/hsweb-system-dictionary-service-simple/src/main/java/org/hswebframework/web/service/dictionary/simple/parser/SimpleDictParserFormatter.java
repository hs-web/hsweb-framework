package org.hswebframework.web.service.dictionary.simple.parser;

import org.hswebframework.web.RegexUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleDictParserFormatter implements DictParserFormatter {

    public boolean smartParse = true;

    public int smartLevel = 3;

    public void setSmartParse(boolean smartParse) {
        this.smartParse = smartParse;
    }

    public boolean needParse(String value, DictParserFormat format) {
        return value.contains(format.getSplitter())
                || value.contains(format.getChildSplitter())
                || value.contains(format.getChildStartChar());
    }

    private <T> FormatterResult<T> createResult(T v, String pattern) {
        return new FormatterResult<>(v, pattern);
    }

    @Override
    public <T> List<FormatterResult<T>> format(DictParserFormat format
            , Object value
            , BiFunction<String, String, T> mapping) {
        if (value == null) return Collections.emptyList();
        String stringValue = String.valueOf(value);
        if (!needParse(stringValue, format))
            return Collections.singletonList(createResult(mapping.apply(stringValue, stringValue), stringValue));

        String splitter = "[" + RegexUtils.escape(format.getSplitter() +
                " " + format.getChildStartChar() +
                " " + format.getChildSplitter() +
                " " + format.getChildEndChar()) +
                "]";
        return Arrays.stream(stringValue.split(splitter))
                .map(val -> {
                    T v = mapping.apply(val, val);
                    if (v == null && smartParse) {
                        StringBuilder tmp = new StringBuilder();
                        char[] arr = val.toCharArray();
                        for (int i = 0; i < arr.length; i++) {
                            tmp.append(arr[i]);
                            if (i >= smartLevel) {
                                v = mapping.apply(tmp.toString(), val);
                                if (null != v) break;
                            }
                        }
                    }
                    return createResult(v, val);
                })
                .collect(Collectors.toList());
    }
}
