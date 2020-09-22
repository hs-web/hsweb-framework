package org.hswebframework.web.dictionary.webflux;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.dict.DictDefine;
import org.hswebframework.web.dict.DictDefineRepository;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.dictionary.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.service.DefaultDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/dictionary")
@Resource(id = "dictionary", name = "数据字典")
@Tag(name = "数据字典管理")
public class WebfluxDictionaryController implements ReactiveServiceCrudController<DictionaryEntity, String> {

    @Autowired
    private DefaultDictionaryService dictionaryService;

    @Autowired
    private DictDefineRepository repository;

    @Override
    public ReactiveCrudService<DictionaryEntity, String> getService() {
        return dictionaryService;
    }

    @GetMapping("/{id:.+}/items")
    @Authorize(merge = false)
    @Operation(summary = "获取数据字段的所有选项")
    public Flux<EnumDict<?>> getItemDefineById(@PathVariable String id) {
        return repository.getDefine(id)
                .flatMapIterable(DictDefine::getItems);
    }

    @GetMapping("/_all")
    @Authorize(merge = false)
    @Schema(description = "获取全部数据字典")
    public Flux<DictDefine> getAllDict() {
        return repository.getAllDefine();
    }
}
