package org.hswebframework.web.dictionary.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.cache.ReactiveCacheManager;
import org.hswebframework.web.dict.DictDefine;
import org.hswebframework.web.dict.defaults.DefaultDictDefineRepository;
import org.hswebframework.web.dictionary.configuration.DictionaryProperties;
import org.hswebframework.web.dictionary.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.event.ClearDictionaryCacheEvent;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public class CompositeDictDefineRepository extends DefaultDictDefineRepository implements SmartInitializingSingleton {


    private final DefaultDictionaryService dictionaryService;

    private final ReactiveCacheManager cacheManager;

    private final DictionaryProperties properties;

    @EventListener
    public void handleClearCacheEvent(ClearDictionaryCacheEvent event) {
        if (StringUtils.hasText(event.getDictionaryId())) {
            cacheManager.<DictDefine>getCache("dic-define")
                        .evict(event.getDictionaryId())
                        .doOnSuccess(r -> log.info("clear dict [{}] cache success", event.getDictionaryId()))
                        .subscribe();
        } else {
            cacheManager.<DictDefine>getCache("dic-define")
                        .clear()
                        .doOnSuccess(r -> log.info("clear all dic cache success"))
                        .subscribe();
        }

    }

    @Override
    public Mono<DictDefine> getDefine(String id) {
        return super.getDefine(id)
                    .switchIfEmpty(Mono.defer(() -> cacheManager
                        .<DictDefine>getCache("dic-define")
                        .getMono(id, () -> getFromDb(id))));
    }

    @Override
    public Flux<DictDefine> getAllDefine() {
        return Flux.concat(super.getAllDefine(), QueryParamEntity
            .newQuery()
            .noPaging()
            .execute(paramEntity -> dictionaryService.findAllDetail(paramEntity, false))
            .map(DictionaryEntity::toDictDefine));
    }

    private Mono<DictDefine> getFromDb(String id) {
        return dictionaryService
            .findDetailById(id)
            .filter(e -> Byte.valueOf((byte) 1).equals(e.getStatus()))
            .map(DictionaryEntity::toDictDefine);
    }


    @Override
    public void afterSingletonsInstantiated() {

        properties
            .doScanEnum()
            .map(CompositeDictDefineRepository::parseEnumDict)
            .forEach(this::addDefine);

    }
}
