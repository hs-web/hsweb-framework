package org.hsweb.web.dao;

import org.hsweb.web.bean.common.InsertParam;

/**
 * @author zhouhao
 */
public interface InsertMapper<Po> {
    int insert(InsertParam<Po> param);
}
