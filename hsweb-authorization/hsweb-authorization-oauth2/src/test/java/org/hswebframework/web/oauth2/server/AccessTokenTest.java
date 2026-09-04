package org.hswebframework.web.oauth2.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AccessTokenTest {

    @Test
    public void shouldPreserveLegacyConstructorWithBearerType() {
        AccessToken token = new AccessToken("access-token", "refresh-token", 60);

        assertEquals("access-token", token.getAccessToken());
        assertEquals("refresh-token", token.getRefreshToken());
        assertEquals(60, token.getExpiresIn());
        assertEquals(AccessToken.DEFAULT_TOKEN_TYPE, token.getTokenType());
        assertNull(token.getScope());

        token.setTokenType(null);
        assertEquals(AccessToken.DEFAULT_TOKEN_TYPE, token.getTokenType());
    }

    @Test
    public void shouldCreateTokenWithExplicitTypeAndScope() {
        AccessToken token = new AccessToken("access-token", null, 30, "DPoP", "read write");

        assertEquals("DPoP", token.getTokenType());
        assertEquals("read write", token.getScope());
    }
}
