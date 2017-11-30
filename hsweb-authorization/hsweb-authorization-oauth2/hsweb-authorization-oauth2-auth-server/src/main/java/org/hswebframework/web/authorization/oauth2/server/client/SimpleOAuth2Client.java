package org.hswebframework.web.authorization.oauth2.server.client;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleOAuth2Client implements OAuth2Client {
    private static final long serialVersionUID = -9179482283099879369L;
    private String id;

    private String secret;

    private String name;

    private String redirectUri;

    private String ownerId;

    private Long createTime;

    private Byte status;

    private Set<String> supportGrantTypes;

    private Set<String> DefaultGrantScope;
}
