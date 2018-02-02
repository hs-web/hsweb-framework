package org.hswebframework.web.organizational.authorization;

import java.io.Serializable;

/**
 * 组织,机构,公司
 *
 * @author zhouhao
 * @since 3.0
 */
public interface Organization extends Serializable {
    /**
     * @return 组织ID, 唯一
     */
    String getId();

    /**
     * @return 组织名称, 如: xxx公司
     */
    String getName();

    /**
     * @return 组织全称, 如: 重庆市xxxx公司
     */
    String getFullName();

    /**
     * @return 组织代码
     */
    String getCode();

    /**
     * @return 所在行政区, 如果未关联将返回<code>null</code>
     */
    District getDistrict();
}
