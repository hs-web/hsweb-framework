package org.hswebframework.web;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;


/**
 * @author zhouhao
 * @since 3.0.6
 */
public class ModuleUtilsTest {

    @Test
    public void test() {
        ModuleUtils.ModuleInfo moduleInfo = ModuleUtils.getModuleByClass(ModuleUtilsTest.class);
        Assert.assertNotNull(moduleInfo);
        Assert.assertFalse(moduleInfo.isNone());
        Assert.assertEquals(moduleInfo.getArtifactId(),"hsweb-commons-utils");
        System.out.println(moduleInfo.getGitLocation());
        System.out.println(moduleInfo.getGitClassLocation(Maps.class,10,12));
        ModuleUtils.ModuleInfo noneInfo = ModuleUtils.getModuleByClass(Logger.class);
        Assert.assertNotNull(noneInfo);
        Assert.assertTrue(noneInfo.isNone());
    }
}