package org.hswebframework.web.dictionary.simple;

import org.hswebframework.web.dict.DictDefine;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.dict.defaults.DefaultDictDefine;
import org.hswebframework.web.dict.defaults.DefaultDictDefineRepository;
import org.hswebframework.web.dictionary.api.DictionaryService;
import org.hswebframework.web.dictionary.api.entity.DictionaryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
@Service
@CacheConfig(cacheNames = "dictionary")
public class BoostDictDefineRepository extends DefaultDictDefineRepository {

    @Autowired
    private DictionaryService dictionaryService;

    @Override
    @Cacheable(key = "'DictDefineById:'+#id")
    public DictDefine getDefine(String id) {
        DictionaryEntity entity = dictionaryService.selectByPk(id);
        if (entity == null) {
            return super.getDefine(id);
        }
        List<EnumDict<Object>> items = (List) new ArrayList<>(entity.getItems());

        return DefaultDictDefine.builder()
                .id(id)
                .comments(entity.getDescribe())
                .items(items)
                .build();
    }

    @Override
    public List<DictDefine> getAllDefine() {
        List<DictDefine> all = dictionaryService.select()
                .stream()
                .map(dict -> DefaultDictDefine.builder()
                        .id(dict.getId())
                        .comments(dict.getDescribe())
                        .items((List) new ArrayList<>(dict.getItems()))
                        .build()).collect(Collectors.toList());

        all.addAll(super.getAllDefine());
        return all;
    }
}
