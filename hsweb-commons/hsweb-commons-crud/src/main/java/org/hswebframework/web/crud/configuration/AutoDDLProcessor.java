package org.hswebframework.web.crud.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
public class AutoDDLProcessor {

    private List<Class> entities = new ArrayList<>();

    @Autowired
    private DatabaseOperator operator;

    @Autowired
    private EasyormProperties properties;

    @Autowired
    private EntityTableMetadataResolver resolver;

    private boolean reactive;

    public void init() {
        if (properties.isAutoDdl()) {
            if(reactive){
                Flux.fromIterable(entities)
                        .doOnNext(type -> log.info("auto ddl for {}", type))
                        .map(resolver::resolve)
                        .flatMap(meta -> operator.ddl().createOrAlter(meta).commit().reactive())
                        .onErrorContinue((err, a) -> log.warn(err.getMessage(), err))
                        .then()
                        .block();
            }else{
                for (Class entity : entities) {
                    log.warn("auto ddl for {}", entity);
                    try {
                        operator.ddl()
                                .createOrAlter(resolver.resolve(entity))
                                .commit()
                                .sync();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
