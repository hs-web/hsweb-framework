package org.hswebframework.web.authorization.simple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.AuthenticationRequest;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlainTextUsernamePasswordAuthenticationRequest implements AuthenticationRequest {
    private String username;

    private String password;
}
