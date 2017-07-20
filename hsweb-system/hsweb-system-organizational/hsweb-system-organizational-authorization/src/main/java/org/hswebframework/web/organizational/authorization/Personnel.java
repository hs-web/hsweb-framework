package org.hswebframework.web.organizational.authorization;

import java.io.Serializable;

/**
 * 人员基本信息
 *
 * @author zhouhao
 * @since 3.0
 */
public interface Personnel extends Serializable {
    String getId();

    String getName();

    String getPhone();

    String getEmail();

    String getPhoto();

}
