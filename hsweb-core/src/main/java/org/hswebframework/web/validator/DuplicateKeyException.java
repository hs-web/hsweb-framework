package org.hswebframework.web.validator;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.BusinessException;

import java.util.Map;

@Getter
@Setter
public class DuplicateKeyException extends BusinessException {

    private Object data;

    public DuplicateKeyException(Object data, String message) {
        super(message, 400);
        this.data = data;
    }
}
