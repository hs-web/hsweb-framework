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

import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.OAuth2ServerConfig;
import org.hswebframework.web.authorization.oauth2.client.simple.OAuth2ServerConfigRepository;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.example.oauth2.github.GithubResponseConvert;
import org.hswebframework.web.example.oauth2.github.GithubResponseJudge;
import org.hswebframework.web.example.oauth2.github.GithubSSOAuthorizingListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 *
 * @author zhouhao
 */
@SpringBootApplication
@Configuration
@EnableCaching
@EnableAspectJAutoProxy
public class OAuth2ClientApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(OAuth2ClientApplication.class);
    }

    @Bean
    public GithubResponseConvert githubResponseConvert() {
        return new GithubResponseConvert();
    }

    @Bean
    public GithubResponseJudge githubResponseJudge() {
        return new GithubResponseJudge();
    }

    @Bean
    public MemoryAuthenticationManager memoryAuthenticationManager() {
        return new MemoryAuthenticationManager();
    }

    @Autowired
    EntityFactory entityFactory;
    @Autowired
    OAuth2ServerConfigRepository repository;
    @Autowired
    OAuth2RequestService oAuth2RequestService;

    @Autowired
    UserTokenManager userTokenManager;

    @Override
    public void run(String... strings) throws Exception {
        //github
        OAuth2ServerConfig github = OAuth2ServerConfig.builder()
                .id("github")
                .name("github test")
                .clientId("b9cd11eae646a5a5c4bf")
                .clientSecret("6b664ebfc051f5919589ccd20cc9e774b026f6f5")
                .apiBaseUrl("https://api.github.com/")
                .authUrl("https://github.com/login/oauth/authorize")
                .accessTokenUrl("https://github.com/login/oauth/access_token")
                .redirectUri("http://localhost:8808/")
                .provider("github")
                .status(DataStatus.STATUS_ENABLED)
                .build();
        repository.save(github);


        OAuth2ServerConfig hsweb = OAuth2ServerConfig.builder()
                .id("hsweb-oauth-server")
                .name("hsweb OAuth2")
                .clientId("hsweb_oauth2_example")
                .clientSecret("hsweb_oauth2_example_secret")
                .apiBaseUrl("http://localhost:8080/")
                .authUrl("oauth2/login.html")
                .accessTokenUrl("oauth2/token")
                .redirectUri("http://localhost:8808/")
                .provider("hsweb")
                .status(DataStatus.STATUS_ENABLED)
                .build();

        repository.save(hsweb);


        OAuth2SSOAuthorizingListener listener = new OAuth2SSOAuthorizingListener(oAuth2RequestService, hsweb.getId(), userTokenManager);

        GithubSSOAuthorizingListener githubSSOAuthorizingListener = new GithubSSOAuthorizingListener(oAuth2RequestService, github.getId(), userTokenManager);

        oAuth2RequestService.registerListener(hsweb.getId(), listener);
        oAuth2RequestService.registerListener(github.getId(), githubSSOAuthorizingListener);
    }


}
