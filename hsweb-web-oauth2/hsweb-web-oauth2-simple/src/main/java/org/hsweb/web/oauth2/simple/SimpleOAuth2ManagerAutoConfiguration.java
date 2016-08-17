/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.oauth2.simple;


import org.hsweb.web.core.authorize.oauth2.OAuth2Manager;
import org.hsweb.web.oauth2.service.OAuth2Service;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.hsweb.web.oauth2")
@MapperScan("org.hsweb.web.oauth2.dao")
public class SimpleOAuth2ManagerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OAuth2Manager.class)
    public OAuth2Manager oAuth2Manager(OAuth2Service oAuth2Service) {
        return new SimpleOAuth2Manager(oAuth2Service);
    }
}
