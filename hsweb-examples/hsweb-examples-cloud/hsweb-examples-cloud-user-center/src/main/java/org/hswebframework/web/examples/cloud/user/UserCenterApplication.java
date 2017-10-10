package org.hswebframework.web.examples.cloud.user;

import org.h2.command.Command;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix
@Configuration
public class UserCenterApplication implements CommandLineRunner{
    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }

    @Autowired
    UserService userService;

    @Override
    public void run(String... strings) throws Exception {
       UserEntity userEntity= userService.createEntity();
       userEntity.setName("super user");
       userEntity.setUsername("admin");
       userEntity.setPassword("admin");

       userService.insert(userEntity);
    }
}