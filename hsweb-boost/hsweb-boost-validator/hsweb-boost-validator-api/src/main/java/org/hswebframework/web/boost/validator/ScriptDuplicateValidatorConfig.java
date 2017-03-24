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

/**
 * 通过脚本来控制数据重复
 *
 * @author zhouhao
 */
public interface ScriptDuplicateValidatorConfig extends DuplicateValidatorConfig {
    default String getType() {
        return DefaultType.SCRIPT;
    }

    /**
     * 脚本语言: javascript(js),groovy
     *
     * @return 语言
     */
    String getScriptLanguage();

    /**
     * 脚本内容,在进行验证的时候会执行脚本,如果存在重复数据脚本应当返回false。否则返回true
     *
     * @return 脚本
     */
    String getScript();

}
