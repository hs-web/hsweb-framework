package org.hswebframework.web.service.form.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@AllArgsConstructor
public class DatabaseInitEvent {

    @Getter
    private DatabaseOperator database;

}
