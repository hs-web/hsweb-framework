package org.hsweb.web.service.form;

import org.hsweb.ezorm.meta.TableMetaData;
import org.hsweb.web.bean.po.form.Form;

import javax.xml.bind.Marshaller;

/**
 * Created by zhouhao on 16-4-20.
 */
public interface FormParser {
    TableMetaData parse(Form form);

    String parseHtml(Form form);

    interface Listener {
        void afterParse(TableMetaData tableMetaData);
    }
}
