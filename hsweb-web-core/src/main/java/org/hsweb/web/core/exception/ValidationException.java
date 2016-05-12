package org.hsweb.web.core.exception;

import org.hsweb.web.bean.valid.ValidResults;

/**
 * Created by zhouhao on 16-5-12.
 */
public class ValidationException extends BusinessException {
    private ValidResults results;

    public ValidationException(String message) {
        super(message, 400);
    }

    public ValidationException(ValidResults results) {
        super(results.toString(), 400);
        this.results = results;
    }

    public ValidResults getResults() {
        return results;
    }
}
