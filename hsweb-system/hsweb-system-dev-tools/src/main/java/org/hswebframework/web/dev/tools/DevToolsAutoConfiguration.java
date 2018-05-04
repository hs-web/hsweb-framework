package org.hswebframework.web.dev.tools;

import org.hswebframework.web.dev.tools.web.FileManagerDevToolsController;
import org.hswebframework.web.dev.tools.writer.CodeWriter;
import org.hswebframework.web.dev.tools.writer.DefaultCodeWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
public class DevToolsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CodeWriter.class)
    public DefaultCodeWriter defaultCodeWriter() {
        return new DefaultCodeWriter();
    }

    @Bean
    public FileManagerDevToolsController fileManagerDevToolsController() {
        return new FileManagerDevToolsController();
    }
}
