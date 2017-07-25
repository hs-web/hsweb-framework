package org.hswebframework.web.workflow.flowable.modeler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.junit.Assert.*;

/**
 * Created by zhouhao on 2017/7/20.
 */
@SpringBootApplication
@ComponentScan("org.flowable.app")
public class FlowableModelTest {

    public static void main(String[] args) {
        SpringApplication.run(FlowableModelTest.class,args);
    }

}