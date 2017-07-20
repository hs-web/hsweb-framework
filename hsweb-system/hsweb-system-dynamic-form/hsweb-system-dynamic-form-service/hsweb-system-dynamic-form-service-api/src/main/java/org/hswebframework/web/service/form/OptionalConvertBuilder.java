package org.hswebframework.web.service.form;

import org.hsweb.ezorm.core.OptionConverter;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface OptionalConvertBuilder {
    OptionConverter buildFromDict(String dictId, String dictParserId);
}
