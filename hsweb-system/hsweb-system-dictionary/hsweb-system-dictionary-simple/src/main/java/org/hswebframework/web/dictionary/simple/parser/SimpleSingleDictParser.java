package org.hswebframework.web.dictionary.simple.parser;

import org.hswebframework.web.ExpressionUtils;
import org.hswebframework.web.dictionary.api.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.api.entity.DictionaryItemEntity;
import org.hswebframework.web.dictionary.api.parser.SingleDictParser;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
public class SimpleSingleDictParser implements SingleDictParser {

    private Map<String, DictMapping> mapping = new HashMap<>();

    private DictParserFormat sourceFormat = new DictParserFormat();

    private DictParserFormat targetFormat = new DictParserFormat();

    private DictParserFormatter formatter = new SimpleDictParserFormatter();

    //设置DictionaryEntity作为配置
    public void setDict(DictionaryEntity dict,
                        Function<DictionaryItemEntity, String> keyGetter,
                        Function<DictionaryItemEntity, String> valueGetter,
                        Function<DictionaryItemEntity, String> expressionGetter) {
        dict.getItems().forEach(item -> addMapping(item, keyGetter, valueGetter, expressionGetter));
    }

    public void setSourceFormat(DictParserFormat sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public void setTargetFormat(DictParserFormat targetFormat) {
        this.targetFormat = targetFormat;
    }

    public DictParserFormat getTargetFormat() {
        return targetFormat;
    }

    public DictParserFormat getSourceFormat() {
        return sourceFormat;
    }

    private DictMapping addMapping(DictionaryItemEntity item,
                                   Function<DictionaryItemEntity, String> keyGetter,
                                   Function<DictionaryItemEntity, String> valueGetter,
                                   Function<DictionaryItemEntity, String> expressionGetter) {
        DictMapping dictMapping = new DictMapping();
        dictMapping.setValue(valueGetter.apply(item));
        dictMapping.setExpression(expressionGetter.apply(item));
        if (item.getChildren() != null) {
            dictMapping.setChildren(item.getChildren().stream()
                    .map(DictionaryItemEntity.class::cast)
                    .map(i -> addMapping(i, keyGetter, valueGetter, expressionGetter)).collect(Collectors.toList()));
        }
        String key = keyGetter.apply(item);
        dictMapping.setKey(key);
        mapping.put(key, dictMapping);
        return dictMapping;
    }

    @Override
    public Optional<String> parse(String value, Object context) {
        if (value == null) {
            return Optional.empty();
        }
        StringJoiner joiner = targetFormat.createJoiner();

        List<DictMapping> dictMappings = formatter
                .format(sourceFormat, value, (key, pattern) -> {
                    DictMapping dictMapping = mapping.get(key);
                    if (dictMapping == null) {
                        return null;
                    }
                    dictMapping = dictMapping.clone();
                    dictMapping.setDefaultVar(Collections.singletonMap("pattern", pattern));
                    return dictMapping;
                })
                .stream()
                .filter(Objects::nonNull)
                .map(FormatterResult::getResult)
                .collect(Collectors.toList());

        Set<String> notAppendList = new HashSet<>();
        List<String> mappingResult = dictMappings.stream()
                .filter(Objects::nonNull)
                // 过滤子节点
                .peek(dictMapping -> dictMapping.filterChildren((mapping -> {
                    String strVal = mapping.getValue();
                    notAppendList.add(strVal); //子节点不拼接
                    int index = dictMappings.indexOf(mappingOfValue(strVal));
                    DictMapping tmp = null;
                    if (-1 != index) {
                        tmp = dictMappings.get(index);
                    }
                    if (null != tmp) {
                        mapping.setDefaultVar(tmp.getDefaultVar());
                    }
                    return null != tmp;
                })))
                .filter(mapping -> !notAppendList.contains(mapping.getValue()))
                //字典转为text
                .map(dict -> dict.toString(context))
                .collect(Collectors.toList());
        mappingResult.forEach(joiner::add);
        return Optional.ofNullable(joiner.toString());
    }

    public void setMapping(Map<String, DictMapping> mapping) {
        this.mapping = mapping;
    }

    DictMapping mappingOfValue(String value) {
        DictMapping mapping = new DictMapping();
        mapping.setValue(value);
        return mapping;
    }

    protected class DictMapping implements Serializable {
        private String key;
        private String value;
        private String expression;
        private String expressionLanguage = "spel";
        private List<DictMapping> children;

        private Map<String, Object> defaultVar;

        public void setDefaultVar(Map<String, Object> defaultVar) {
            this.defaultVar = defaultVar;
        }

        public Map<String, Object> getDefaultVar() {
            return defaultVar;
        }

        @Override
        public int hashCode() {
            if (value == null) {
                return 0;
            }
            return value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            return obj == this || hashCode() == obj.hashCode();
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public List<DictMapping> getChildren() {
            return children;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public void setExpressionLanguage(String expressionLanguage) {
            this.expressionLanguage = expressionLanguage;
        }

        public String getExpressionLanguage() {
            if (expressionLanguage == null) {
                expressionLanguage = "spel";
            }
            return expressionLanguage;
        }

        public void setChildren(List<DictMapping> children) {
            this.children = children;
        }

        public String toString(Function<DictMapping, String> getter) {
            StringBuilder stringBuilder = new StringBuilder(getter.apply(this));
            if (children != null) {
                //根据getter 获取子节点的string
                String childrenString = String.join(targetFormat.getChildSplitter(), children.stream()
                        .map(mapping -> mapping.toString(getter)).collect(Collectors.toList()));

                if (childrenString.isEmpty()) {
                    return stringBuilder.toString();
                }
                //拼接子节点
                stringBuilder.append(targetFormat.getChildStartChar())
                        .append(childrenString)
                        .append(targetFormat.getChildEndChar());
            }
            return stringBuilder.toString();
        }

        public String toString(Object context) {
            Function<DictMapping, String> textGetter =
                    context == null ? DictMapping::getValue :
                            dictMapping -> {
                                if (dictMapping.getExpression() == null || dictMapping.getExpression().isEmpty()) {
                                    return dictMapping.getValue();
                                }
                                // 解析表达式
                                Map<String, Object> var = new HashMap<>();
                                if (dictMapping.getDefaultVar() != null) {
                                    var.putAll(dictMapping.getDefaultVar());
                                }
                                var.put("value", dictMapping.getValue());
                                var.put("key", dictMapping.getKey());
                                var.put("context", context);
                                var.put("children", dictMapping.getChildren());
                                try {
                                    return ExpressionUtils.analytical(dictMapping.getExpression(), var, dictMapping.getExpressionLanguage());
                                } catch (Exception e) {
                                    throw new RuntimeException("analytical " + dictMapping.getExpressionLanguage() + " expression :" + dictMapping.getExpression() + " error", e);
                                }
                            };
            return toString(textGetter);
        }

        public String toTextString() {
            return toString(DictMapping::getValue);
        }

        public String toValueString() {
            return toString(mapping -> String.valueOf(mapping.getValue()));
        }

        @Override
        public DictMapping clone() {
            DictMapping clone = new DictMapping();
            clone.value = value;
            clone.key = key;
            clone.expression = expression;
            clone.expressionLanguage = expressionLanguage;
            if (children != null) {
                clone.children = children.stream().map(DictMapping::clone).collect(Collectors.toList());
            }
            return clone;
        }

        public void filterChildren(Predicate<DictMapping> mappingPredicate) {
            if (children != null) {
                children = children.stream()
                        .filter(mappingPredicate)
                        .collect(Collectors.toList());
                children.forEach(children -> children.filterChildren(mappingPredicate));
            }
        }
    }
}
