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


import java.io.Serializable;

/**
 * 重复数据验证配置
 *
 * @author zhouhao
 */
public interface DuplicateValidatorConfig extends Serializable {
    /**
     * 对数据的操作事件
     *
     * @return 操作事件
     * @see org.hswebframework.web.authorization.Permission#ACTION_UPDATE
     * @see org.hswebframework.web.authorization.Permission#ACTION_ADD
     */
    String getAction();

    /**
     * @return 验证未通过时返回的消息
     */
    String getErrorMessage();

    /**
     * @return 验证方式
     */
    String getType();

    interface DefaultType {
        String SCRIPT = "SCRIPT";
        String FIELDS = "FIELDS";
        String CUSTOM = "CUSTOM";
    }
}
