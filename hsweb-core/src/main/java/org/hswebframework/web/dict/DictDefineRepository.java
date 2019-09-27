package org.hswebframework.web.dict;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0
 */
public interface DictDefineRepository {
    DictDefine getDefine(String id);

    List<DictDefine> getAllDefine();

    void addDefine(DictDefine dictDefine);
}
