package org.hswebframework.web.authorization.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhouhao on 2017/8/30.
 */
public class JwtTokenParser implements UserTokenParser {

    private JwtConfig jwtConfig;

    public JwtTokenParser(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public String parseToken(HttpServletRequest request) {
        String headerToken = request.getHeader("jwt-token");
        if(StringUtils.isEmpty(headerToken)){
            headerToken=request.getHeader("Authorization");
            if(!StringUtils.isEmpty(headerToken)){
                if(headerToken.contains(" ")){
                    String[] auth =headerToken.split("[ ]");
                   // if(auth[0].equalsIgnoreCase("jwt")){
                        headerToken=auth[1];
                    //}
                }
            }
        }
        if(headerToken!=null){
           return parseJWT(headerToken).getSubject();
        }
        return null;
    }

    public Claims parseJWT(String jwt){
        SecretKey key = jwtConfig.generalKey();
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt).getBody();
        return claims;
    }


}
