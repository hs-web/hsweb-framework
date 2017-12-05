package org.hswebframework.web.oauth2;

import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.hswebframework.web.authorization.oauth2.server.token.AccessTokenService;
import org.hswebframework.web.oauth2.authorization.OAuth2UserTokenParser;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@ConditionalOnClass(UserTokenParser.class)
//@Configuration
//@AutoConfigureAfter(OAuth2GranterAutoConfiguration.class)
public class OAuth2AuthorizationAutoConfiguration {

//    @Bean
    public OAuth2UserTokenParser oAuth2UserTokenParser(AccessTokenService accessTokenService) {
        return new OAuth2UserTokenParser(accessTokenService);
    }
}
