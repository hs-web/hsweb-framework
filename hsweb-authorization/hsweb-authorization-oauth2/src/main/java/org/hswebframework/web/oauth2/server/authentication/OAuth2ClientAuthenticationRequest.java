package org.hswebframework.web.oauth2.server.authentication;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Normalized input for one OAuth2 client authentication attempt.
 *
 * <p>The request contains transport-neutral metadata and a safe parameter view. Concrete request
 * types own their authentication evidence and erase sensitive state when authentication terminates.
 * Implementations must not retain the request beyond the asynchronous authentication operation.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see ReactiveOAuth2ClientAuthenticator
 */
@Getter
public abstract class OAuth2ClientAuthenticationRequest {

    public static final String CLIENT_SECRET_BASIC = "client_secret_basic";

    public static final String CLIENT_SECRET_POST = "client_secret_post";

    private final String clientId;

    private final String authenticationMethod;

    private final String grantType;

    private final Map<String, String> parameters;

    /**
     * Create the context for one client authentication attempt.
     *
     * @param clientId client identifier extracted by the token endpoint
     * @param authenticationMethod authentication mechanism used to obtain the credentials
     * @param grantType requested OAuth2 grant type
     * @param parameters request parameters after credential fields have been removed
     */
    protected OAuth2ClientAuthenticationRequest(String clientId,
                                                String authenticationMethod,
                                                String grantType,
                                                Map<String, String> parameters) {
        this.clientId = clientId;
        this.authenticationMethod = authenticationMethod;
        this.grantType = grantType;
        this.parameters = safeParameters(parameters);
    }

    /**
     * Erase credentials owned by this request.
     *
     * <p>The token endpoint invokes this method when the authentication publisher completes,
     * fails, is cancelled, or throws synchronously. Implementations must make it idempotent and
     * non-throwing, and clear every sensitive copy they own.</p>
     */
    public void eraseCredentials() {
    }

    private static Map<String, String> safeParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> safe = new LinkedHashMap<>(parameters);
        safe.remove("client_secret");
        return Collections.unmodifiableMap(safe);
    }
}
