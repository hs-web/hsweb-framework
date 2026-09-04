package org.hswebframework.web.oauth2.server.credential;

import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthentication;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ClientCredentialRequestCompatibilityTest {

    @Test
    public void shouldKeepOriginalClientOnlyForLegacyConstructor() {
        OAuth2Client source = client();

        ClientCredentialRequest request =
            new ClientCredentialRequest(source, Collections.emptyMap());

        assertSame(source, request.getClient());
        assertEquals("secret", request.getClient().getClientSecret());
        assertNotSame(source, request.getClientAuthentication().getClient());
        assertNull(request.getClientAuthentication().getClient().getClientSecret());
        assertEquals(source.getClientId(),
                     request.getClientAuthentication().getClient().getClientId());
    }

    @Test
    public void shouldExposeSanitizedClientForAuthenticationConstructor() {
        OAuth2Client source = client();
        OAuth2ClientAuthentication authentication =
            new OAuth2ClientAuthentication(source, "api", Collections.emptyMap());

        ClientCredentialRequest request =
            new ClientCredentialRequest(authentication, Collections.emptyMap());

        assertSame(authentication, request.getClientAuthentication());
        assertSame(authentication.getClient(), request.getClient());
        assertNotSame(source, request.getClient());
        assertNull(request.getClient().getClientSecret());
        assertEquals("secret", source.getClientSecret());
    }

    private OAuth2Client client() {
        OAuth2Client client = new OAuth2Client();
        client.setClientId("client-id");
        client.setClientSecret("secret");
        client.setName("client name");
        client.setDescription("client description");
        client.setRedirectUrl("https://example.com/callback");
        client.setUserId("user-id");
        return client;
    }
}
