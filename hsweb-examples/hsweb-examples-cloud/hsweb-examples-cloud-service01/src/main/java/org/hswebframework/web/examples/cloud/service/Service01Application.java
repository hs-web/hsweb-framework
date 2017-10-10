package org.hswebframework.web.examples.cloud.service;


import feign.Feign;
import org.hswebframework.web.authorization.cloud.feign.FeignUserTokenManager;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix
@Configuration
@EnableFeignClients("org.hswebframework.web.authorization.cloud.feign")
public class Service01Application {

//    @Bean
//   public UserTokenManager userTokenManager(){
//      return Feign.builder().target(FeignUserTokenManager.class,"http://localhost:9000");
//    }


    public static void main(String[] args) {
        SpringApplication.run(Service01Application.class, args);
    }
}