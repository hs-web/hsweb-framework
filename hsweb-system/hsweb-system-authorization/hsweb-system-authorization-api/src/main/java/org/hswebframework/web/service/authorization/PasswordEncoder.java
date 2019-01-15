/*
 *  Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.service.authorization;

/**
 * 密码编码器,用于将明文密码编码成密文
 *
 * @author zhouhao
 * @since 3.0
 */
public interface PasswordEncoder {

    /**
     * 编码,相同的参数,编码的结果永远相同.
     *
     * @param password 明文密码,不能为<code>null</code>
     * @param salt     加密盐
     * @return 加密结果
     */
    String encode(String password, String salt);
}
