package org.hsweb.web.service.form;

import org.hsweb.ezorm.run.Table;

import java.util.Map;

public interface DynamicFormDataValidator {
    String getRepeatDataId(Table table, Map<String, Object> data);

}
