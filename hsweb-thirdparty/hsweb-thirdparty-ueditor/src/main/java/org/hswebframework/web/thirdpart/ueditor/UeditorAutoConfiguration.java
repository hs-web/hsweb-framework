package org.hswebframework.web.thirdpart.ueditor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UeditorAutoConfiguration {

    @Bean
    public UeditorController ueditorController(){
        return new UeditorController();
    }
}
