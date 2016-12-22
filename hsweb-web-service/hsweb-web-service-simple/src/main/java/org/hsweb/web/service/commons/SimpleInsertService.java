package org.hsweb.web.service.commons;

import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.dao.InsertMapper;
import org.hsweb.web.service.InsertService;

/**
 * @author zhouhao
 */
public interface SimpleInsertService<Po extends GenericPo<Pk>, Pk> extends InsertService<Po, Pk> {

    InsertMapper<Po> getInsertMapper();

    void tryValidPo(Po data);

    Class<Pk> getPKType();

    @Override
    default Pk insert(Po data) {
        if (getPKType() == String.class && data.getId() == null) {
            ((GenericPo<String>) data).setId(GenericPo.createUID());
        }
        tryValidPo(data);
        getInsertMapper().insert(InsertParam.build(data));
        return data.getId();
    }
}
