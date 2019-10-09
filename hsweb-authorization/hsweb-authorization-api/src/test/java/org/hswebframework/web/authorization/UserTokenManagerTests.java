package org.hswebframework.web.authorization;

import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.token.*;
import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;

public class UserTokenManagerTests {


    /**
     * 基本功能测试
     * @throws InterruptedException  Thread.sleep error
     */
    @Test
    public void testDefaultSetting() throws InterruptedException {
        DefaultUserTokenManager userTokenManager = new DefaultUserTokenManager();
        userTokenManager.setAllopatricLoginMode(AllopatricLoginMode.allow); //允许异地登录

        UserToken userToken = userTokenManager.signIn("test", "sessionId", "admin", 1000).block();
        Assert.assertNotNull(userToken);

        //可重复登录
        userTokenManager.signIn("test2", "sessionId", "admin", 30000).block();

        //2个token
        userTokenManager.totalToken()
                .as(StepVerifier::create)
                .expectNext(2)
                .verifyComplete();

        //1个用户
        userTokenManager.totalUser()
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        //改变token状态
        userTokenManager.changeUserState("admin", TokenState.deny).subscribe();

        userToken = userTokenManager.getByToken(userToken.getToken()).block();

        Assert.assertEquals(userToken.getState(), TokenState.deny);

        userTokenManager.changeUserState("admin", TokenState.effective).subscribe();

        Thread.sleep(1200);

        userTokenManager.getByToken(userToken.getToken())
                .map(UserToken::isExpired)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        userTokenManager.checkExpiredToken().subscribe();


        userTokenManager.getByToken(userToken.getToken())
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();

        userTokenManager.totalToken()
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

        userTokenManager.totalUser()
                .as(StepVerifier::create)
                .expectNext(1)
                .verifyComplete();

    }


    /**
     * 测试异地登录模式之禁止登录
     */
    @Test
    public void testDeny() throws InterruptedException {
        DefaultUserTokenManager userTokenManager = new DefaultUserTokenManager();
        userTokenManager.setAllopatricLoginMode(AllopatricLoginMode.deny);//如果在其他地方登录，本地禁止登录

        userTokenManager.signIn("test", "sessionId", "admin", 10000).subscribe();

        try {
            userTokenManager.signIn("test2", "sessionId", "admin", 30000).block();
            Assert.assertTrue(false);
        } catch (AccessDenyException e) {

        }
        Assert.assertTrue(userTokenManager.getByToken("test").block().isNormal());
        Assert.assertNull(userTokenManager.getByToken("test2").block());

    }

    /**
     * 测试异地登录模式之踢下线
     */
    @Test
    public void testOffline()   {
        DefaultUserTokenManager userTokenManager = new DefaultUserTokenManager();
        userTokenManager.setAllopatricLoginMode(AllopatricLoginMode.offlineOther); //将其他地方登录的用户踢下线

        userTokenManager.signIn("test", "sessionId", "admin", 1000).subscribe();

        userTokenManager.signIn("test2", "sessionId", "admin", 30000).subscribe();

        Assert.assertTrue(userTokenManager.getByToken("test2").block().isNormal());

        Assert.assertTrue(userTokenManager.getByToken("test").block().isOffline());

    }


}
