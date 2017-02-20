/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.authorization.annotation;

import java.lang.annotation.*;

/**
 * 用于springmvc中,将授权信息自动注入到参数中.
 * 例如:
 * <pre>
 *    &#064;ReuqestMapping("/example")
 *    public ResponseMessage(&#064;AuthInfo Authorization auth){
 *      User user = auth.getUser();
 *      return ok();
 *    }
 * </pre>
 *
 * @author zhouhao
 * @see org.hswebframework.web.authorization.Authorization
 * @since 3.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthInfo {
}
