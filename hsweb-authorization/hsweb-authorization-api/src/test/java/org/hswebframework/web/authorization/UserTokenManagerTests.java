package org.hswebframework.web.authorization;

import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.token.*;
import org.junit.Assert;
import org.junit.Test;

public class UserTokenManagerTests {


    /**
     * 基本功能测试
     * @throws InterruptedException  Thread.sleep error
     */
    @Test
    public void testDefaultSetting() throws InterruptedException {
        DefaultUserTokenManager userTokenManager = new DefaultUserTokenManager();
        userTokenManager.setAllopatricLoginMode(AllopatricLoginMode.allow); //允许异地登录

        UserToken userToken = userTokenManager.signIn("test", "sessionId", "admin", 1000);
        Assert.assertNotNull(userToken);

        //可重复登录
        userTokenManager.signIn("test2", "sessionId", "admin", 30000);
        Assert.assertEquals(userTokenManager.totalToken(), 2); //2个token
        Assert.assertEquals(userTokenManager.totalUser(), 1);//1个用户

        //改变token状态
        userTokenManager.changeUserState("admin", TokenState.deny);

        userToken = userTokenManager.getByToken(userToken.getToken());

        Assert.assertEquals(userToken.getState(), TokenState.deny);

        userTokenManager.changeUserState("admin", TokenState.effective);

        Thread.sleep(1200);

        userToken = userTokenManager.getByToken(userToken.getToken());
        Assert.assertTrue(userToken.isExpired());

        userTokenManager.checkExpiredToken();

        userToken = userTokenManager.getByToken(userToken.getToken());
        Assert.assertTrue(userToken == null);
        Assert.assertEquals(userTokenManager.totalToken(), 1);
        Assert.assertEquals(userTokenManager.totalUser(), 1);

    }


    /**
     * 测试异地登录模式之禁止登录
     */
    @Test
    public void testDeny() throws InterruptedException {
        DefaultUserTokenManager userTokenManager = new DefaultUserTokenManager();
        userTokenManager.setAllopatricLoginMode(AllopatricLoginMode.deny);//如果在其他地方登录，本地禁止登录

        userTokenManager.signIn("test", "sessionId", "admin", 10000);

        try {
            userTokenManager.signIn("test2", "sessionId", "admin", 30000);
            Assert.assertTrue(false);
        } catch (AccessDenyException e) {

        }
        Assert.assertTrue(userTokenManager.getByToken("test").isNormal());
        Assert.assertTrue(userTokenManager.getByToken("test2")==null);

    }

    /**
     * 测试异地登录模式之踢下线
     */
    @Test
    public void testOffline()   {
        DefaultUserTokenManager userTokenManager = new DefaultUserTokenManager();
        userTokenManager.setAllopatricLoginMode(AllopatricLoginMode.offlineOther); //将其他地方登录的用户踢下线

        userTokenManager.signIn("test", "sessionId", "admin", 1000);

        userTokenManager.signIn("test2", "sessionId", "admin", 30000);

        Assert.assertTrue(userTokenManager.getByToken("test2").isNormal());

        Assert.assertTrue(userTokenManager.getByToken("test").isOffline());

    }


}
