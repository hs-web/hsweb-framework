package org.hsweb.controller.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 浩 on 2016-01-16 0016.
 */
@RestController
@RequestMapping(value = "/test")
@EnableAutoConfiguration
public class HelloWorld {

    @RequestMapping
    public String test() {
        return "hello-world";
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorld.class);
    }
}
