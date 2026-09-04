package org.hswebframework.web.oauth2.server.credential;

import lombok.Getter;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2Request;
import org.hswebframework.web.oauth2.server.authentication.OAuth2ClientAuthentication;

import java.util.Map;

/**
 * Client-credentials grant input passed to the selected granter or typed handler.
 *
 * <p>The legacy constructor preserves the supplied {@link OAuth2Client} reference for trusted
 * programmatic compatibility and therefore may expose fields present on that object. Token endpoint
 * integrations should use the authenticated-context constructor with parameters already sanitized
 * by the HTTP boundary; that path exposes the secret-free client projection from
 * {@link OAuth2ClientAuthentication}. Except for removed sensitive values such as
 * {@code client_secret}, parameters remain request data and must not be used to derive the client
 * identity or type. Handlers must use {@link #getClientAuthentication()} as the trusted
 * authentication result.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 * @see ClientCredentialGrantHandler
 * @see OAuth2ClientAuthentication
 */
@Getter
public class ClientCredentialRequest extends OAuth2Request {

    private final OAuth2Client client;

    private final OAuth2ClientAuthentication clientAuthentication;

    /**
     * Create a legacy request for trusted programmatic callers.
     *
     * <p>This constructor retains the supplied client reference and does not sanitize its fields or
     * the parameter map. It must not be used to pass untrusted HTTP input.</p>
     *
     * @param client trusted client object used by the legacy granter
     * @param parameters trusted grant parameters
     */
    public ClientCredentialRequest(OAuth2Client client, Map<String, String> parameters) {
        super(parameters);
        this.client = client;
        this.clientAuthentication = new OAuth2ClientAuthentication(client);
    }

    /**
     * Create a request from an authenticated, secret-free client context.
     *
     * <p>The token endpoint uses this constructor after removing {@code client_secret} from the
     * parameters. Callers outside that boundary are responsible for supplying an equally sanitized
     * map. The remaining parameters are still request data: neither client identity nor
     * {@code clientType} may be derived from them. Handlers must route and authorize using
     * {@link #getClientAuthentication()}.</p>
     *
     * @param clientAuthentication non-null authenticated client context
     * @param parameters grant parameters that have already been sanitized
     */
    public ClientCredentialRequest(OAuth2ClientAuthentication clientAuthentication,
                                   Map<String, String> parameters) {
        super(parameters);
        this.clientAuthentication = clientAuthentication;
        this.client = clientAuthentication.getClient();
    }
}
