package org.hswebframework.web.dictionary.webflux;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.dictionary.entity.DictionaryItemEntity;
import org.hswebframework.web.dictionary.service.DefaultDictionaryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/dictionary-item")
@Resource(id = "dictionary", name = "数据字典")
@Tag(name = "数据字典选项管理")
public class WebfluxDictionaryItemController implements ReactiveServiceCrudController<DictionaryItemEntity, String> {

    @Autowired
    private DefaultDictionaryItemService dictionaryItemService;

    @Override
    public ReactiveCrudService<DictionaryItemEntity, String> getService() {
        return dictionaryItemService;
    }

}
