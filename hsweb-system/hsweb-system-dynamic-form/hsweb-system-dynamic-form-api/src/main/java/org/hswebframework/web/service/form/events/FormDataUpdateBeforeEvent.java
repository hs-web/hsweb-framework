package org.hswebframework.web.service.form.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@AllArgsConstructor
@Getter
public class FormDataUpdateBeforeEvent<T> {
    private String formId;

    private T data;

    private Object id;
}
