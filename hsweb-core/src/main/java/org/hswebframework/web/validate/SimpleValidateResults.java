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

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 */
public class SimpleValidateResults implements ValidateResults {

    private static final long                         serialVersionUID = -3355828475840578917L;
    private              List<ValidateResults.Result> results          = new ArrayList<>();

    public SimpleValidateResults addResult(String field, String message) {
        results.add(new Result(field, message));
        return this;
    }

    public SimpleValidateResults addResult(ValidateResults.Result result) {
        results.add(result);
        return this;
    }

    @Override
    public boolean isSuccess() {
        return results == null || results.isEmpty();
    }

    @Override
    public List<ValidateResults.Result> getResults() {
        return results;
    }

    class Result implements ValidateResults.Result {
        private static final long   serialVersionUID = -4717219071013488363L;
        private              String field;
        private              String message;

        public Result(String field, String message) {
            this.field = field;
            this.message = message;
        }

        @Override
        public String getField() {
            return field;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "{" +
                    "\"field\":\"" + field + '\"' +
                    ", \"message:\"" + message + '\"' +
                    '}';
        }
    }

    @Override
    public String toString() {
        if (isSuccess()) {
            return "success";
        }
        return results.toString();
    }
}
