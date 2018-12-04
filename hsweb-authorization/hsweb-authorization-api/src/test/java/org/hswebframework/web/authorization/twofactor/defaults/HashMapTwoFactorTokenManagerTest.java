package org.hswebframework.web.authorization.twofactor.defaults;

import lombok.SneakyThrows;
import org.hswebframework.web.authorization.twofactor.TwoFactorToken;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public class HashMapTwoFactorTokenManagerTest {

    HashMapTwoFactorTokenManager tokenManager = new HashMapTwoFactorTokenManager();

    @Test
    @SneakyThrows
    public void test() {
        TwoFactorToken twoFactorToken = tokenManager.getToken("test", "test");

        Assert.assertTrue(twoFactorToken.expired());
        twoFactorToken.generate(1000L);
        Assert.assertFalse(twoFactorToken.expired());
        Thread.sleep(1100);
        Assert.assertTrue(twoFactorToken.expired());
    }
}