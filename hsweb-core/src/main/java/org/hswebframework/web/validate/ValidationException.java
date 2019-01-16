/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.validate;


import org.hswebframework.web.BusinessException;

import java.util.Collections;
import java.util.List;

public class ValidationException extends BusinessException {
    private static final long serialVersionUID = 7807607467371210082L;
    private ValidateResults results;

    public ValidationException(String message) {
        super(message, 400);
    }

    public ValidationException(String message, String field) {
        super(message, 400);
        results = new SimpleValidateResults().addResult(field, message);
    }

    public ValidationException(ValidateResults results) {
        super(results.getResults().get(0).getMessage(), 400);
        this.results = results;
    }

    public List<ValidateResults.Result> getResults() {
        if (results == null) {
            return new java.util.ArrayList<>();
        }
        return results.getResults();
    }
}
