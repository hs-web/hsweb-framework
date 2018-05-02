package org.hswebframework.web.id;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhouhao
 * @since 3.0
 */
public class IDGeneratorTests {

    @Test
    public void test() {
        System.setProperty("id-worker","1");
        System.setProperty("id-datacenter","1");

        Assert.assertNotNull(IDGenerator.UUID.generate());
        Assert.assertNotNull(IDGenerator.MD5.generate());
        Assert.assertNotNull(IDGenerator.RANDOM.generate());
        Assert.assertNotNull(IDGenerator.SNOW_FLAKE.generate());
        Assert.assertNotNull(IDGenerator.SNOW_FLAKE_HEX.generate());

        for (int i = 0; i < 100; i++) {
            System.out.println(IDGenerator.SNOW_FLAKE.generate());
        }
    }
}
