package org.hswebframework.web.workflow.starter;

import com.alibaba.fastjson.parser.ParserConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.hswebframework.web.workflow")
public class WorkFlowAutoConfiguration implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        ParserConfig.getGlobalInstance()
                .addAccept("org.hswebframework.web.workflow.dao.entity");
    }
}
