package org.hswebframework.web.validator;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.BusinessException;

import java.util.Map;

@Getter
@Setter
public class DuplicateKeyException extends BusinessException {

    private static final long serialVersionUID = 8449360528527473673L;
    private LogicPrimaryKeyValidator.Result result;

    public DuplicateKeyException(LogicPrimaryKeyValidator.Result result) {
        this(result, result.getMessage());
    }

    public DuplicateKeyException(LogicPrimaryKeyValidator.Result result, String message) {
        super(message, 400);
        this.result = result;
    }
}
