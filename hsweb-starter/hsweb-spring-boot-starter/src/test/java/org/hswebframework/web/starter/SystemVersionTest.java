package org.hswebframework.web.starter;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author zhouhao
 * @since 3.0.8
 */
public class SystemVersionTest {

    @Test
    public void test() {
        SystemVersion version = new SystemVersion();

        version.setVersion("3.0-RELEASE");
        Assert.assertEquals(version.getMajorVersion(), 3);
        Assert.assertEquals(version.getMinorVersion(), 0);
        Assert.assertEquals(version.getRevisionVersion(), 0);

        version.setVersion("3.0.1-RELEASE");
        Assert.assertEquals(version.getMajorVersion(), 3);
        Assert.assertEquals(version.getMinorVersion(), 0);
        Assert.assertEquals(version.getRevisionVersion(), 1);
        version.setVersion("3.2.1-SNAPSHOT");
        Assert.assertEquals(version.getMajorVersion(), 3);
        Assert.assertEquals(version.getMinorVersion(), 2);
        Assert.assertEquals(version.getRevisionVersion(), 1);
        Assert.assertTrue(version.isSnapshot());

        version.setVersion("3.1.2");
        Assert.assertEquals(version.getMajorVersion(), 3);
        Assert.assertEquals(version.getMinorVersion(), 1);
        Assert.assertEquals(version.getRevisionVersion(), 2);

        version.setVersion("3.1.2.RELEASE");
        Assert.assertEquals(version.getMajorVersion(), 3);
        Assert.assertEquals(version.getMinorVersion(), 1);
        Assert.assertEquals(version.getRevisionVersion(), 2);

    }


}