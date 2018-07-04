package org.hswebframework.web.service.form.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.rdb.RDBTable;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@AllArgsConstructor
@Getter
public class FormDataInsertBeforeEvent<T> {
    private String formId;

    private RDBTable<T> table;

    private T data;

}
