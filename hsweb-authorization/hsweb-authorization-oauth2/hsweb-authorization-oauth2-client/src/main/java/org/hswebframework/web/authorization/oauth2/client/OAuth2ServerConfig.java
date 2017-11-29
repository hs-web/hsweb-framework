package org.hswebframework.web.authorization.oauth2.client;

import lombok.*;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2ServerConfig implements Serializable {
    private static final long serialVersionUID = 2915370625863707033L;
    private String id;
    //服务名称
    private String name;
    //api根地址
    private String apiBaseUrl;
    //认证地址
    private String authUrl;
    //token获取地址
    private String accessTokenUrl;
    //客户端id
    private String clientId;
    //客户端密钥
    private String clientSecret;
    //是否启用
    private Byte status;
    //重定向地址
    private String redirectUri;
    //服务提供商
    private String provider;

}
