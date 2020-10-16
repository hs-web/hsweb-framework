package org.hswebframework.web.oauth2.server.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationCodeCache implements Serializable {
    private static final long serialVersionUID = -6849794470754667710L;

    private String clientId;

    private String code;

    private Authentication authentication;

    private String scope;

}