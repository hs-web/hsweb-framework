package org.hswebframework.web.oauth2.server;

import org.junit.Test;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;

import static org.junit.Assert.*;

public class OAuth2ClientTest {

    @Test
    public void shouldNormalizeLegacyClientTypeToDefault() {
        OAuth2Client client = new OAuth2Client();

        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, client.getClientType());

        client.setClientType(null);
        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, client.getClientType());

        client.setClientType("");
        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, client.getClientType());

        client.setClientType(" ");
        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, client.getClientType());

        client.setClientType("api");
        assertEquals("api", client.getClientType());
    }

    @Test
    public void shouldAllowCompatibleRedirectVariants() {
        OAuth2Client client = createClient("http://hsweb.me/callback");
        client.validateRedirectUri("http://hsweb.me/callback");
        client.validateRedirectUri("http://hsweb.me/callback?a=1&n=1");
        client.validateRedirectUri("http://hsweb.me/callback/next");
    }

    @Test
    public void shouldAllowSubPathWhenRegisteredUrlIsOrigin() {
        createClient("https://trusted.example.com")
                .validateRedirectUri("https://trusted.example.com/callback");
    }

    @Test
    public void shouldRejectRedirectUriUserInfoBypass() {
        assertIllegalRedirect(
                createClient("https://trusted.example.com"),
                "https://trusted.example.com:password@evil.com/callback"
        );
    }

    @Test
    public void shouldRejectSiblingPathWithSamePrefix() {
        assertIllegalRedirect(
                createClient("http://hsweb.me/callback"),
                "http://hsweb.me/callback2"
        );
    }

    @Test
    public void shouldRejectDifferentHost() {
        assertIllegalRedirect(
                createClient("http://hsweb.me/callback"),
                "http://evil.com/callback"
        );
    }

    @Test
    public void shouldRejectFragmentRedirectUri() {
        assertIllegalRedirect(
                createClient("http://hsweb.me/callback"),
                "http://hsweb.me/callback#code"
        );
    }

    @Test
    public void shouldRequireExactRedirectUriInExactMode() {
        OAuth2Client client = createClient("http://hsweb.me/callback");
        client.validateRedirectUri(
                "http://hsweb.me/callback",
                OAuth2Properties.RedirectUriValidationMode.EXACT
        );
        createClient("http://hsweb.me/callback?a=1")
                .validateRedirectUri(
                        "http://hsweb.me/callback?a=1",
                        OAuth2Properties.RedirectUriValidationMode.EXACT
                );
    }

    @Test
    public void shouldRejectCompatibleOnlyRedirectInExactMode() {
        OAuth2Client client = createClient("http://hsweb.me/callback");
        assertIllegalRedirect(
                client,
                "http://hsweb.me/callback/next",
                OAuth2Properties.RedirectUriValidationMode.EXACT
        );
        assertIllegalRedirect(
                createClient("http://hsweb.me/callback?a=1"),
                "http://hsweb.me/callback?a=1&n=1",
                OAuth2Properties.RedirectUriValidationMode.EXACT
        );
    }

    private OAuth2Client createClient(String redirectUrl) {
        OAuth2Client client = new OAuth2Client();
        client.setRedirectUrl(redirectUrl);
        return client;
    }

    private void assertIllegalRedirect(OAuth2Client client, String redirectUri) {
        assertIllegalRedirect(client, redirectUri, OAuth2Properties.RedirectUriValidationMode.COMPATIBLE);
    }

    private void assertIllegalRedirect(OAuth2Client client,
                                       String redirectUri,
                                       OAuth2Properties.RedirectUriValidationMode validationMode) {
        try {
            client.validateRedirectUri(redirectUri, validationMode);
            fail("expected redirect uri to be rejected");
        } catch (OAuth2Exception e) {
            assertEquals(ErrorType.ILLEGAL_REDIRECT_URI, e.getType());
        }
    }
}
