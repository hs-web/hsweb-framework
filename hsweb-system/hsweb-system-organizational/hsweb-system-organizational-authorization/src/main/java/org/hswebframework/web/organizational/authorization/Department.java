package org.hswebframework.web.organizational.authorization;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface Department extends Serializable {
    String getId();

    String getName();

    String getCode();

    Organization getOrg();
}
