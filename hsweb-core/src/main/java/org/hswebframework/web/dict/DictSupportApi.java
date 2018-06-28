package org.hswebframework.web.dict;

/**
 * @author zhouhao
 * @since 3.0
 */
@Deprecated
public interface DictSupportApi {
    DictParser getParser(String id, String defaultId);

    default DictParser getParser(String id) {
        return getParser(id, "default");
    }

    <T> T wrap(T target);

    <T> T unwrap(T target);

}
