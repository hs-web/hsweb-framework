package org.hswebframework.web.dict.apply;

import org.hswebframework.web.dict.DictDefineRepository;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface DictWrapper {
    DictWrapper empty = (bean, repository) -> {};

    void wrap(Object bean, DictDefineRepository repository);


}
