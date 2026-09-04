package org.hswebframework.web.oauth2.server.authentication;

import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DefaultReactiveOAuth2ClientAuthenticatorTest {

    @Test
    public void shouldAuthenticateLegacyClient() {
        OAuth2Client client = client("test", "secret");
        DefaultReactiveOAuth2ClientAuthenticator authenticator =
            new DefaultReactiveOAuth2ClientAuthenticator(id -> Mono.just(client));

        authenticator
            .authenticate(request("test", "secret"))
            .as(StepVerifier::create)
            .assertNext(authentication -> {
                assertNotSame(client, authentication.getClient());
                assertEquals(client.getClientId(), authentication.getClient().getClientId());
                assertNull(authentication.getClient().getClientSecret());
                assertEquals("secret", client.getClientSecret());
                assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE,
                             authentication.getClientType());
                assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE,
                             authentication.getClient().getClientType());
                assertEquals(OAuth2ClientAuthenticationRequest.CLIENT_SECRET_POST,
                             authentication.getAuthenticationMethod());
                assertTrue(authentication.getAttributes().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    public void shouldPropagateTrustedClientTypeToSanitizedAuthenticationContext() {
        OAuth2Client client = client("test", "secret");
        client.setClientType("api");
        DefaultReactiveOAuth2ClientAuthenticator authenticator =
            new DefaultReactiveOAuth2ClientAuthenticator(id -> Mono.just(client));
        Map<String, String> untrustedParameters = new HashMap<>();
        untrustedParameters.put("client_type", "untrusted");
        untrustedParameters.put("clientType", "untrusted");
        OAuth2ClientAuthenticationRequest request = new OAuth2ClientSecretAuthenticationRequest(
            "test",
            OAuth2ClientAuthenticationRequest.CLIENT_SECRET_POST,
            "secret".toCharArray(),
            "client_credentials",
            untrustedParameters);

        authenticator
            .authenticate(request)
            .as(StepVerifier::create)
            .assertNext(authentication -> {
                assertEquals("api", authentication.getClientType());
                assertEquals("api", authentication.getClient().getClientType());
                assertNotSame(client, authentication.getClient());
                assertNull(authentication.getClient().getClientSecret());
                assertEquals("secret", client.getClientSecret());
                assertEquals(OAuth2ClientAuthenticationRequest.CLIENT_SECRET_POST,
                             authentication.getAuthenticationMethod());
            })
            .verifyComplete();
    }

    @Test
    public void shouldRejectUnknownClientWithLegacyError() {
        DefaultReactiveOAuth2ClientAuthenticator authenticator =
            new DefaultReactiveOAuth2ClientAuthenticator(id -> Mono.empty());

        authenticator
            .authenticate(request("missing", "secret"))
            .as(StepVerifier::create)
            .expectErrorMatches(error -> error instanceof OAuth2Exception
                && ((OAuth2Exception) error).getType() == ErrorType.ILLEGAL_CLIENT_ID)
            .verify();
    }

    @Test
    public void shouldRejectInvalidSecretWithLegacyError() {
        OAuth2Client client = client("test", "secret");
        DefaultReactiveOAuth2ClientAuthenticator authenticator =
            new DefaultReactiveOAuth2ClientAuthenticator(id -> Mono.just(client));

        authenticator
            .authenticate(request("test", "wrong"))
            .as(StepVerifier::create)
            .expectErrorMatches(error -> error instanceof OAuth2Exception
                && ((OAuth2Exception) error).getType() == ErrorType.ILLEGAL_CLIENT_SECRET)
            .verify();
    }

    @Test
    public void shouldRejectMissingSecretWithLegacyError() {
        OAuth2Client client = client("test", "secret");
        DefaultReactiveOAuth2ClientAuthenticator authenticator =
            new DefaultReactiveOAuth2ClientAuthenticator(id -> Mono.just(client));
        OAuth2ClientAuthenticationRequest request = new OAuth2ClientSecretAuthenticationRequest(
            "test",
            OAuth2ClientAuthenticationRequest.CLIENT_SECRET_POST,
            null,
            "client_credentials",
            Collections.emptyMap());

        authenticator
            .authenticate(request)
            .as(StepVerifier::create)
            .expectErrorMatches(error -> error instanceof OAuth2Exception
                && ((OAuth2Exception) error).getType() == ErrorType.ILLEGAL_CLIENT_SECRET)
            .verify();
    }

    @Test
    public void shouldProtectCredentialsAndAuthenticationContext() {
        char[] credentials = "secret".toCharArray();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_secret", "secret");
        parameters.put("scope", "read");

        OAuth2ClientSecretAuthenticationRequest request = new OAuth2ClientSecretAuthenticationRequest(
            "test",
            OAuth2ClientAuthenticationRequest.CLIENT_SECRET_POST,
            credentials,
            "client_credentials",
            parameters);

        credentials[0] = 'X';
        parameters.put("scope", "write");
        char[] exposed = request.getClientSecret();
        exposed[0] = 'Y';

        assertArrayEquals("secret".toCharArray(), request.getClientSecret());
        assertEquals("read", request.getParameters().get("scope"));
        assertFalse(request.getParameters().containsKey("client_secret"));
        assertFalse(request.toString().contains("secret"));
        try {
            request.getParameters().put("scope", "write");
            fail("parameters must be immutable");
        } catch (UnsupportedOperationException ignore) {
            // expected
        }
        request.eraseCredentials();
        assertNull(request.getClientSecret());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("credentialId", "credential-1");
        attributes.put("client_secret", "secret");
        OAuth2Client client = client("test", "secret");
        client.setName("client name");
        client.setDescription("client description");
        client.setRedirectUrl("https://example.com/callback");
        client.setUserId("user-id");
        OAuth2ClientAuthentication authentication =
            new OAuth2ClientAuthentication(client, "api", attributes);
        attributes.put("credentialId", "credential-2");

        assertNotSame(client, authentication.getClient());
        assertEquals("test", authentication.getClient().getClientId());
        assertEquals("client name", authentication.getClient().getName());
        assertEquals("client description", authentication.getClient().getDescription());
        assertEquals("https://example.com/callback", authentication.getClient().getRedirectUrl());
        assertEquals("user-id", authentication.getClient().getUserId());
        assertNull(authentication.getClient().getClientSecret());
        assertEquals("secret", client.getClientSecret());
        assertEquals("api", authentication.getClientType());
        assertEquals("api", authentication.getClient().getClientType());
        assertNull(authentication.getAuthenticationMethod());
        assertEquals("credential-1", authentication.getAttributes().get("credentialId"));
        assertFalse(authentication.getAttributes().containsKey("client_secret"));
        try {
            authentication.getAttributes().put("credentialId", "credential-2");
            fail("attributes must be immutable");
        } catch (UnsupportedOperationException ignore) {
            // expected
        }

        assertInvalidClientType(client, null);
        assertInvalidClientType(client, " ");
        assertInvalidAuthenticationMethod(client, " ");
    }

    private OAuth2ClientAuthenticationRequest request(String clientId, String secret) {
        return new OAuth2ClientSecretAuthenticationRequest(
            clientId,
            OAuth2ClientAuthenticationRequest.CLIENT_SECRET_POST,
            secret.toCharArray(),
            "client_credentials",
            Collections.singletonMap("client_secret", secret));
    }

    private OAuth2Client client(String clientId, String secret) {
        OAuth2Client client = new OAuth2Client();
        client.setClientId(clientId);
        client.setClientSecret(secret);
        return client;
    }

    private void assertInvalidClientType(OAuth2Client client, String clientType) {
        try {
            new OAuth2ClientAuthentication(client, clientType, Collections.emptyMap());
            fail("clientType must be rejected");
        } catch (IllegalArgumentException ignore) {
            // expected
        }
    }

    private void assertInvalidAuthenticationMethod(OAuth2Client client, String authenticationMethod) {
        try {
            new OAuth2ClientAuthentication(
                client,
                "api",
                authenticationMethod,
                Collections.emptyMap());
            fail("blank authenticationMethod must be rejected");
        } catch (IllegalArgumentException ignore) {
            // expected
        }
    }
}
