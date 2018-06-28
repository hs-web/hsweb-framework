package org.hswebframework.web.dict;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface DictParser {
    String getId();

    String parseText(DictDefine dictDefine, Object value);

    String parseValue(DictDefine dictDefine, String text);
}
