package org.hswebframework.web.service.form.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.rdb.RDBDatabase;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@AllArgsConstructor
public class DatabaseInitEvent {

    @Getter
    private RDBDatabase database;

}
