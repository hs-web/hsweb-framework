package org.hswebframework.web.dashboard.starter;

import org.hswebframework.web.dao.Dao;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
@ComponentScan({"org.hswebframework.web.dashboard.local"
        , "org.hswebframework.web.controller.dashboard"})
public class DashboardAutoConfiguration {
}
