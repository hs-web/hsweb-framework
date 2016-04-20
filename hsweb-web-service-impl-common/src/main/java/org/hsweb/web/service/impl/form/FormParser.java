package org.hsweb.web.service.impl.form;

import org.hsweb.web.bean.po.form.Form;
import org.webbuilder.sql.TableMetaData;

/**
 * Created by zhouhao on 16-4-20.
 */
public interface FormParser {
    TableMetaData parse(Form form);
}
