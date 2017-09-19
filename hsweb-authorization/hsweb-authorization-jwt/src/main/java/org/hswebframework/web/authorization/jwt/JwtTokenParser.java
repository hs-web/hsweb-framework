package org.hswebframework.web.authorization.jwt;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhouhao on 2017/8/30.
 */
public class JwtTokenParser implements UserTokenParser {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenParser.class);

    private JwtConfig jwtConfig;

    public JwtTokenParser(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {
        String headerToken = request.getHeader("jwt-token");
        if (StringUtils.isEmpty(headerToken)) {
            headerToken = request.getHeader("Authorization");
            if (!StringUtils.isEmpty(headerToken)) {
                if (headerToken.contains(" ")) {
                    String[] auth = headerToken.split("[ ]");
                    // if(auth[0].equalsIgnoreCase("jwt")){
                    headerToken = auth[1];
                    //}
                }
            }
        }
        if (headerToken != null) {
            try {
                Claims claims = parseJWT(headerToken);
                if (claims.getExpiration().getTime() <= System.currentTimeMillis()) {

                    return null;
                }
                return JSON.parseObject(claims.getSubject(), DefaultAuthorizedToken.class);
            } catch (Exception e) {
                logger.error("parse token [{}] error", headerToken, e);
                return null;
            }
        }
        return null;
    }

    public Claims parseJWT(String jwt) {
        SecretKey key = jwtConfig.generalKey();
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt).getBody();
    }


}
