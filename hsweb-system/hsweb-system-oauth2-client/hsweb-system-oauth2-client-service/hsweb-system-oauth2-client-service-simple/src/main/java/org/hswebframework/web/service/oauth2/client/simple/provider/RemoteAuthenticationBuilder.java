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

package org.hswebframework.web.service.oauth2.client.simple.provider;

import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.FieldAccessConfig;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class RemoteAuthenticationBuilder {

    public static Authentication fromJson(String json) {
        return JSONObject.parseObject(json, SimpleAuthentication.class);
    }
}
