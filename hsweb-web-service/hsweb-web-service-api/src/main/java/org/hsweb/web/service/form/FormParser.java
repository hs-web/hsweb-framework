package org.hsweb.web.service.form;

import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hsweb.web.bean.po.form.Form;


public interface FormParser {
    RDBTableMetaData parse(Form form);

    String parseHtml(Form form);

    interface Listener {
        void afterParse(RDBTableMetaData tableMetaData);
    }
}
