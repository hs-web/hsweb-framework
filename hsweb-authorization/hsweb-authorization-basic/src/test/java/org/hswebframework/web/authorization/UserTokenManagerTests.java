package org.hswebframework.web.authorization;

import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.junit.Assert;
import org.junit.Test;

public class UserTokenManagerTests {

    protected UserTokenManager userTokenManager = new DefaultUserTokenManager();


    public void setUserTokenManager(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @Test
    public void simpleTest() throws InterruptedException {
        UserToken userToken = userTokenManager.signIn("test", "sessionId", "admin", 1000);

        Assert.assertNotNull(userToken);

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
    }


}
