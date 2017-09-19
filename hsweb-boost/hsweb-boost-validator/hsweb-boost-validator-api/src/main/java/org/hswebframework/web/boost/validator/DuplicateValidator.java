/*
 *  Copyright 2016 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.boost.validator;

import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;

/**
 * 重复数据验证器,验证数据是否重复
 *
 * @author zhouhao
 */
public interface DuplicateValidator {
    Result doValidate(DuplicateValidatorConfig validator, MethodInterceptorContext context);

    /**
     * 验证结果
     */
    class Result {
        //是否存在重复的数据
        boolean exists;
        //存在的数据,不存在时值为null
        Object  data;

        public Result() {
        }

        public Result(boolean exists, Object data) {
            this.exists = exists;
            this.data = data;
        }

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
