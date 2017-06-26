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

package org.hswebframework.web.example.oauth2;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RestController
@RequestMapping("/")
public class IndexController {

    @GetMapping
    public ResponseMessage index(Authentication authentication) {
        return ResponseMessage.ok(authentication);
    }

    @GetMapping("/test")
    @Authorize(role = "admin")
    public ResponseMessage auth() {
        return ResponseMessage.ok("admin角色");
    }

    @GetMapping("/test2")
    @Authorize(role = "admin2")//此角色应该是不存在的
    public ResponseMessage auth2() {
        return ResponseMessage.ok("admin2角色");
    }
}
