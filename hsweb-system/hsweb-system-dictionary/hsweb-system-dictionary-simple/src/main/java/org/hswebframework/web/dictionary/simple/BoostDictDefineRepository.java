package org.hswebframework.web.dictionary.simple;

import org.hswebframework.web.dict.ClassDictDefine;
import org.hswebframework.web.dict.DictDefine;
import org.hswebframework.web.dict.ItemDefine;
import org.hswebframework.web.dict.defaults.DefaultDictDefine;
import org.hswebframework.web.dict.defaults.DefaultDictDefineRepository;
import org.hswebframework.web.dict.defaults.DefaultItemDefine;
import org.hswebframework.web.dictionary.api.DictionaryService;
import org.hswebframework.web.dictionary.api.entity.DictionaryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
        List<ItemDefine> items = entity.getItems()
                .stream()
                .map(item -> DefaultItemDefine
                        .builder()
                        .value(item.getValue())
                        .text(item.getText())
                        .comments(item.getDescribe())
                        .build())
                .collect(Collectors.toList());

        return DefaultDictDefine.builder()
                .id(id)
                .comments(entity.getDescribe())
                .items(items)
                .build();
    }

    @Override
    @Cacheable(key = "'DictDefineByClass:'+#type.name")
    public List<ClassDictDefine> getDefine(Class type) {
        return super.getDefine(type);
    }
}
