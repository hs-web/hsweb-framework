package org.hswebframework.web.service.config;

import org.hswebframework.web.service.config.simple.SimpleConfigService;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class ConfigServiceTests {
    public static void main(String[] args) {
        SimpleConfigService configService =new SimpleConfigService();
        System.out.println(new SimpleConfigService().createBean());
    }
}
