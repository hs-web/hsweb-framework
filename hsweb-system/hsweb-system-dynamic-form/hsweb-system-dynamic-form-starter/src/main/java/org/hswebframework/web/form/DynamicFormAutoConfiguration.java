package org.hswebframework.web.form;

import org.hswebframework.web.service.form.DynamicFormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
public class DynamicFormAutoConfiguration {

    public static class DynamicFormAutoDeploy implements ApplicationListener<ContextStartedEvent> {

        private Logger logger = LoggerFactory.getLogger(this.getClass());

        @Autowired
        private DynamicFormService dynamicFormService;

        @Override
        public void onApplicationEvent(ContextStartedEvent event) {
            try {
                dynamicFormService.deployAllFromLog();
            } catch (Exception e) {
                logger.error("deploy form error", e);
            }
        }
    }
}
