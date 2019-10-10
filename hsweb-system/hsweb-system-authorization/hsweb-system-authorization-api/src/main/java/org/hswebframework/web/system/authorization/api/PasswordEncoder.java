package org.hswebframework.web.system.authorization.api;

public interface PasswordEncoder {

    String encode(String password, String salt);
}
