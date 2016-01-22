package org.hsweb.controller.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by 浩 on 2016-01-16 0016.
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = {"org.hsweb.web"})
public class Run {
    public static void main(String[] args) {
        SpringApplication.run(Run.class);
    }
}
