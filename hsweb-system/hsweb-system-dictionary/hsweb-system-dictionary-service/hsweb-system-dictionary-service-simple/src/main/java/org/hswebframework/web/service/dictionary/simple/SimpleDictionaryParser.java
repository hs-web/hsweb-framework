package org.hswebframework.web.service.dictionary.simple;

import org.hswebframework.web.ExpressionUtils;
import org.hswebframework.web.entity.dictionary.DictionaryEntity;
import org.hswebframework.web.entity.dictionary.DictionaryItemEntity;
import org.hswebframework.web.service.dictionary.DictionaryParser;
import org.hswebframework.web.service.dictionary.simple.parser.SimpleSingleDictParser;
import org.hswebframework.web.service.dictionary.simple.parser.SingleDictParser;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 简单的字典解析器实现,支持树形结构字典
 * <p>
 * e.g.
 * <pre>
 *   //字典
 *   [
 *    {text:"苹果",value:1,
 *     children:[
 *      {text:"青苹果",value:101},
 *      {text:"红富士",value:102},
 *      {text:"其他苹果",value:103,textExpression:"其他苹果(${#context.otherApple})"}
 *    ]}
 *    {text:"梨子",value:2}
 *   ]
 *   //调用
 *   parser.valueToText("1,101,103",{otherApple:"其他苹果1"});
 *   //返回结果  苹果(青苹果,其他苹果(其他苹果1))
 *
 *   //调用
 *   parser.textToValue("苹果(青苹果,其他苹果)")
 *   //返回结果 1,101,103
 * </pre>
 *
 * @author zhouhao
 * @since 3.0
 */
public class SimpleDictionaryParser<V> implements DictionaryParser<V> {

    private SimpleSingleDictParser toTextParser = new SimpleSingleDictParser();

    private SimpleSingleDictParser toValueParser = new SimpleSingleDictParser();

    public SimpleSingleDictParser getToTextParser() {
        return toTextParser;
    }

    public SimpleSingleDictParser getToValueParser() {
        return toValueParser;
    }

    //设置DictionaryEntity作为配置
    public void setDict(DictionaryEntity<? extends DictionaryItemEntity> dict) {
        toTextParser.setDict(dict, DictionaryItemEntity::getValue
                , DictionaryItemEntity::getText
                , DictionaryItemEntity::getTextExpression);

        toValueParser.setDict(dict, DictionaryItemEntity::getText
                , DictionaryItemEntity::getValue,
                DictionaryItemEntity::getValueExpression);

        toValueParser.getTargetFormat().setSplitter(",");
        toValueParser.getTargetFormat().setChildStartChar(",");
        toValueParser.getTargetFormat().setChildEndChar("");
        toValueParser.getTargetFormat().setChildSplitter(",");
    }

    @Override
    public Optional<String> valueToText(V value, Object context) {
        if (value == null) return Optional.empty();
        return toTextParser.parse(String.valueOf(value), context);
    }

    @Override
    public Optional<V> textToValue(String text, Object context) {
        return toValueParser.parse(text, context).map(v -> (V) v);
    }
}
