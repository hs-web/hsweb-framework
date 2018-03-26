package org.hswebframework.web.eventbus.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApplication {

    @Bean
    public Test2 test2(){
        return new Test2();
    }
}
