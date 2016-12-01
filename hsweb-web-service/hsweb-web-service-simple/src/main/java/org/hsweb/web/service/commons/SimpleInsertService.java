package org.hsweb.web.service.commons;

import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.dao.InsertMapper;
import org.hsweb.web.service.InsertService;

/**
 * @author zhouhao
 */
public interface SimpleInsertService<Po, Pk> extends InsertService<Po, Pk> {

    InsertMapper<Po> getInsertMapper();

    void tryValidPo(Po data);

    @Override
    default Pk insert(Po data) {
        Pk primaryKey = null;
        if (data instanceof GenericPo) {
            if (((GenericPo) data).getId() == null)
                ((GenericPo) data).setId(GenericPo.createUID());
            primaryKey = (Pk) ((GenericPo) data).getId();
        }
        tryValidPo(data);
        getInsertMapper().insert(InsertParam.build(data));
        return primaryKey;
    }
}
