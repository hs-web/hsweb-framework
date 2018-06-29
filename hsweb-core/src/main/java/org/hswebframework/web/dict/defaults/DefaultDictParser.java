package org.hswebframework.web.dict.defaults;

import org.hswebframework.web.dict.DictDefine;
import org.hswebframework.web.dict.DictParser;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.dict.ItemDefine;

import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
public class DefaultDictParser implements DictParser {
    @Override
    public String getId() {
        return "default";
    }

    @Override
    public String parseText(DictDefine dictDefine, Object value) {
        return dictDefine.getItems()
                .stream()
                .filter(itemDefine -> itemDefine.eq(value))
                .map(EnumDict::getText)
                .collect(Collectors.joining(","));
    }

    @Override
    public String parseValue(DictDefine dictDefine, String text) {
        return dictDefine.getItems()
                .stream()
                .filter(itemDefine -> itemDefine.eq(text))
                .map(EnumDict::getValue)
                .map(String::valueOf)
                .findFirst()
                .orElse(text);
    }
}
