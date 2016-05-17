package org.hsweb.web.service.form;

import java.util.Map;

/**
 * Created by zhouhao on 16-5-16.
 */
public interface DynamicFormDataValidator {
    String getRepeatDataId(String tableName, Map<String, Object> data);
}
