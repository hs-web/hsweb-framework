package org.hswebframework.web.service.form;

import org.hswebframework.ezorm.core.DictionaryCodec;
import org.hswebframework.ezorm.core.ValueCodec;
import org.hswebframework.web.entity.form.DictConfig;

/**
 *
 * @author zhouhao
 */
public interface OptionalConvertBuilder {
    DictionaryCodec build(DictConfig dictConfig);

    ValueCodec buildValueConverter(DictConfig dictConfig);
}
