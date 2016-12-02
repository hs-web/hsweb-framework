package org.hsweb.web.service.form;


import org.hsweb.ezorm.rdb.RDBTable;

import java.util.Map;

public interface DynamicFormDataValidator {
    String getRepeatDataId(RDBTable table, Map<String, Object> data);

}
