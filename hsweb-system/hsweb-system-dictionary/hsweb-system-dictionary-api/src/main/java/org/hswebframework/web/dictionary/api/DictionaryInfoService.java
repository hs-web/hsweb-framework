package org.hswebframework.web.dictionary.api;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface DictionaryInfoService  {
    int insert(List<DictionaryInfo> dictionaryInfo);

    List<DictionaryInfo> select(String targetKey, String targetId, String dictionaryId);

    int delete(String targetKey, String targetId, String dictionaryId);
}
