package org.hswebframework.web.starter.easyorm;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.springframework.beans.factory.annotation.Autowired;

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
