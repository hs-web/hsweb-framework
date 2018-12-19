package org.hswebframework.web.authorization.twofactor;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public interface TwoFactorToken extends Serializable {
    void generate(long timeout);

    boolean expired();
}
