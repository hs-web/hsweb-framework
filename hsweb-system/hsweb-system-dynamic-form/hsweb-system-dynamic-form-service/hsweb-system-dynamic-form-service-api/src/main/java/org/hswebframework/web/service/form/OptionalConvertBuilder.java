package org.hswebframework.web.service.form;

import org.hswebframework.ezorm.core.OptionConverter;
import org.hswebframework.web.entity.form.DynamicFormColumnEntity;

/**
 *
 * @author zhouhao
 */
public interface OptionalConvertBuilder {
    OptionConverter build(DynamicFormColumnEntity columnEntity);
}
