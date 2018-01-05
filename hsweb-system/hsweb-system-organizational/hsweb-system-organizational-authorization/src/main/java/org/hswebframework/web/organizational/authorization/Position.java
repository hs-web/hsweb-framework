package org.hswebframework.web.organizational.authorization;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface Position extends Serializable {
    String getId();

    String getCode();

    String getName();

    Department getDepartment();

}
