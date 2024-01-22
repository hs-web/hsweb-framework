package org.hswebframework.web.dictionary.service;

import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.dictionary.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.entity.DictionaryItemEntity;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
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

    public DictionaryItemEntity createItem(String value) {
        DictionaryItemEntity itemEntity = new DictionaryItemEntity();
        itemEntity.setDictId("demo");
        itemEntity.setName(value);
        itemEntity.setValue(value);
        itemEntity.setText(value);
        itemEntity.setStatus((byte) 1);
        return itemEntity;
    }

    @Test
    public void save() {
        DictionaryItemEntity itemEntity = createItem("test1");
        itemEntity.setOrdinal(0);
        DictionaryItemEntity itemEntity2 = createItem("test2");
        itemEntity2.setOrdinal(0);

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
                .expectNext(1L)
                .verifyComplete();

        itemEntity2.setOrdinal(null);

        defaultDictionaryItemService
                .save(Flux.just(itemEntity, itemEntity2))
                .then()
                .as(StepVerifier::create)
                .expectErrorMessage("error.ordinal_can_not_null")
                .verify();

    }

    @Test
    public void testErrorOrdinal() {
        DictionaryItemEntity itemEntity = createItem("test-error");
        itemEntity.setOrdinal(0);

        defaultDictionaryItemService
                .save(itemEntity)
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        DictionaryItemEntity itemEntity2 = createItem("test-error");
        itemEntity2.setOrdinal(0);

        defaultDictionaryItemService
                .insert(itemEntity2)
                .then()
                .as(StepVerifier::create)
                .expectError(DuplicateKeyException.class)
                .verify();

    }

    @Test
    public void testAutoOrdinal() {
        //自动填充ordinal
        DictionaryItemEntity itemEntity = createItem("test-auto");
        itemEntity.setOrdinal(null);
        DictionaryItemEntity itemEntity2 = createItem("test-auto");
        itemEntity2.setOrdinal(null);


        defaultDictionaryItemService
                .save(Flux.just(itemEntity, itemEntity2))
                .then()
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        defaultDictionaryItemService
                .query(QueryParamEntity.of("value","test-auto").noPaging())
                .doOnNext(System.out::println)
                .count()
                .as(StepVerifier::create)
                .expectNext(2L)
                .verifyComplete();
    }

}