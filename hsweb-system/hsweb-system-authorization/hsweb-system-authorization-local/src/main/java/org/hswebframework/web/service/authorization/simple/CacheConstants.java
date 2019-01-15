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

package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.authorization.AuthenticationManager;

/**
 * 缓存所需常量
 *
 * @author zhouhao
 */
public interface CacheConstants {
    String MENU_CACHE_NAME = "hsweb-menu-";

    String USER_MENU_CACHE_NAME = "hsweb-user-menu-";

    String USER_CACHE_NAME = "user-";

    String USER_AUTH_CACHE_NAME = AuthenticationManager.USER_AUTH_CACHE_NAME;

}
