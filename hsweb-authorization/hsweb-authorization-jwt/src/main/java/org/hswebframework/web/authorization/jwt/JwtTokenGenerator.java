package org.hswebframework.web.authorization.jwt;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.basic.web.GeneratedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenGenerator;
import org.hswebframework.web.id.IDGenerator;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hswebframework.web.authorization.jwt.JwtAuthorizedToken.TOKEN_TYPE;

public class JwtTokenGenerator implements UserTokenGenerator {

    private JwtConfig jwtConfig;

    public JwtTokenGenerator(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public String getSupportTokenType() {
        return TOKEN_TYPE;
    }

    private String createToken() {
        return IDGenerator.MD5.generate();
    }

    @Override
    public GeneratedToken generate(Authentication authentication) {
        String token = createToken();
        String userId = authentication.getUser().getId();

        String subject = JSON.toJSONString(new JwtAuthorizedToken(token, userId));

        String jwtToken = createJWT(jwtConfig.getId(), subject, jwtConfig.getTtl());

//        String refreshToken = createJWT(jwtConfig.getId(), userId, jwtConfig.getRefreshTtl());

        int timeout = jwtConfig.getTtl();

        return new GeneratedToken() {
            private static final long serialVersionUID = -4362122360342275321L;

            @Override
            public Map<String, Object> getResponse() {
                Map<String, Object> map = new HashMap<>();
                map.put("token", jwtToken);
//                map.put("refreshToken", refreshToken);
                return map;
            }

            @Override
            public String getToken() {
                return token;
            }

            @Override
            public String getType() {
                return TOKEN_TYPE;
            }

            @Override
            public int getTimeout() {
                return timeout;
            }
        };
    }


    public String createJWT(String id, String subject, long ttlMillis) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        SecretKey key = jwtConfig.generalKey();
        JwtBuilder builder = Jwts.builder()
                .setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .signWith(signatureAlgorithm, key);
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }
}
