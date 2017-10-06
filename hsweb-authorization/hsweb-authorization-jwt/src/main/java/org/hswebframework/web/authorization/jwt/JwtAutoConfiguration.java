package org.hswebframework.web.authorization.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
public class JwtAutoConfiguration  {

    @Bean
    @ConfigurationProperties(prefix = "hsweb.authorize.jwt")
    public JwtConfig jwtConfig(){
        return new JwtConfig();
    }

    @Bean
    public JwtTokenGenerator jwtTokenGenerator(JwtConfig config){
        return new JwtTokenGenerator(config);
    }

    @Bean
    public JwtTokenParser jwtTokenParser(JwtConfig config){
        return new JwtTokenParser(config);
    }
}
