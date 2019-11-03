package org.hswebframework.web.dict;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author zhouhao
 * @since 1.0
 */
public interface DictDefineRepository {
    Mono<DictDefine> getDefine(String id);

    Flux<DictDefine> getAllDefine();

    void addDefine(DictDefine dictDefine);
}
