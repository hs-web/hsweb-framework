package org.hswebframework.web.service.dictionary.simple.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.service.dictionary.builder.DictionaryParserBuilder;
import org.hswebframework.web.service.dictionary.parser.SingleDictParser;
import org.hswebframework.web.service.dictionary.simple.parser.SimpleSingleDictParser;

import java.util.Objects;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleDictionaryParserBuilder implements DictionaryParserBuilder {
    @Override
    public SingleDictParser build(String config) {
        Objects.requireNonNull(config, "config is null");
        JSONObject object = JSON.parseObject(config);
        String type = object.getString("type");
        switch (type) {
            case "simple":
                return object.getObject("parser", SimpleSingleDictParser.class);
            case "script":
                // TODO: 17-5-25
            default:
                throw new UnsupportedOperationException(config);
        }
    }
}
