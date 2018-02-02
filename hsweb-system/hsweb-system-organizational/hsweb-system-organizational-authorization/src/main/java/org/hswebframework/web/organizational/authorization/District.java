package org.hswebframework.web.organizational.authorization;

import java.io.Serializable;

/**
 * 行政区域
 *
 * @author zhouhao
 * @since 3.0
 */
public interface District extends Serializable {
    /**
     * @return 行政区域ID
     */
    String getId();

    /**
     * @return 行政区域名称, 如:江津区
     */
    String getName();

    /**
     * @return 行政区域全名, 如:重庆市江津区
     */
    String getFullName();

    /**
     * @return 行政区域代码, 如: 500116
     */
    String getCode();
}
