package org.hswebframework.web.service.dsl;


import org.hswebframework.web.commons.entity.Entity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DSLQuery<PO> extends Entity {
    List<PO> list();

    List<PO> list(int pageIndex, int pageSize);

    PO single();

    int count();
}
