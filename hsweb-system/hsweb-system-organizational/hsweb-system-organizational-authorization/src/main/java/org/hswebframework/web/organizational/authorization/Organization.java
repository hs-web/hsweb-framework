package org.hswebframework.web.organizational.authorization;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface Organization extends Serializable {
    String getId();

    String getName();

    String getFullName();

    String getCode();

    District getDistrict();
}
