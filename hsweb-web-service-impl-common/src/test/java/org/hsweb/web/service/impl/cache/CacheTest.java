package org.hsweb.web.service.impl.cache;

import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.impl.AbstractTestCase;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * Created by zhouhao on 16-4-26.
 */
public class CacheTest extends AbstractTestCase {

    @Resource
    private ConfigService configService;

    @Test
    public void testCache() {
        configService.get("test", "test", "11");
        System.out.println(configService.get("test", "test", "11"));
        System.out.println(configService.get("test", "test", "11"));
        System.out.println(configService.get("test", "test", "11"));
    }
}
