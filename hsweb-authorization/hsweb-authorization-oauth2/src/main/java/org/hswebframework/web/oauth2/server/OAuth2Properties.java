package org.hswebframework.web.oauth2.server;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "hsweb.oauth2")
@Getter
@Setter
public class OAuth2Properties {

    //token有效期
    private Duration tokenExpireIn = Duration.ofSeconds(7200);

    //refreshToken有效期
    private Duration refreshTokenIn = Duration.ofDays(30);

}
