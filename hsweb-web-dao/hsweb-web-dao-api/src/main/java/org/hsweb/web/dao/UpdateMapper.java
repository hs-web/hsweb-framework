package org.hsweb.web.dao;

import org.hsweb.web.bean.common.UpdateParam;

/**
 * @author zhouhao
 */
public interface UpdateMapper<Po> {
    /**
     * 修改记录信息
     *
     * @param data 要修改的对象
     * @return 影响记录数
     */
    int update(UpdateParam<Po> data);
}
