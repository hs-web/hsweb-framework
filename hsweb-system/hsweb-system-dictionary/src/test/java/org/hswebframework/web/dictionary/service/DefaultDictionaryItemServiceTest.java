package org.hswebframework.web.dictionary.service;

import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.dictionary.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.entity.DictionaryItemEntity;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class DefaultDictionaryItemServiceTest {

    @Autowired
    private DefaultDictionaryItemService defaultDictionaryItemService;
    @Autowired
    private DefaultDictionaryService defaultDictionaryService;

    @BeforeEach
    void init() {
        DictionaryEntity dictionary = new DictionaryEntity();
        dictionary.setName("demo");
        dictionary.setStatus((byte) 1);
        dictionary.setId("demo");

        defaultDictionaryService
                .save(dictionary)
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    @Test
    public void save() {
        DictionaryItemEntity itemEntity = new DictionaryItemEntity();
        itemEntity.setDictId("demo");
        itemEntity.setName("item1");
        itemEntity.setValue("item1");
        itemEntity.setText("item1");
        itemEntity.setOrdinal(0);
        itemEntity.setStatus((byte) 1);

        DictionaryItemEntity itemEntity2 = new DictionaryItemEntity();
        itemEntity2.setDictId("demo");
        itemEntity2.setName("item2");
        itemEntity2.setValue("item2");
        itemEntity2.setText("item2");
        itemEntity2.setOrdinal(0);
        itemEntity2.setStatus((byte) 1);

        defaultDictionaryItemService
                .save(Flux.just(itemEntity, itemEntity2))
                .then()
                .as(StepVerifier::create)
                .expectError(DuplicateKeyException.class)
                .verify();

        itemEntity2.setOrdinal(null);

        defaultDictionaryItemService
                .save(Flux.just(itemEntity, itemEntity2))
                .then()
                .as(StepVerifier::create)
                .expectErrorMessage("error.ordinal_can_not_null")
                .verify();

        itemEntity2.setOrdinal(1);

        defaultDictionaryItemService
                .save(Flux.just(itemEntity, itemEntity2))
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();


        //自动填充ordinal
        itemEntity.setId(null);
        itemEntity.setOrdinal(null);
        itemEntity2.setId(null);
        itemEntity2.setOrdinal(null);

        defaultDictionaryItemService
                .save(Flux.just(itemEntity, itemEntity2))
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        defaultDictionaryItemService
                .query(new QueryParamEntity().noPaging())
                .doOnNext(System.out::println)
                .count()
                .as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();


    }

}