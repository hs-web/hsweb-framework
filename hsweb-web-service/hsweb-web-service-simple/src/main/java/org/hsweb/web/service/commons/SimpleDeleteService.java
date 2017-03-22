package org.hsweb.web.service.commons;

import org.hsweb.ezorm.core.dsl.Delete;
import org.hsweb.web.bean.common.DeleteParam;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.dao.DeleteMapper;
import org.hsweb.web.service.DeleteService;
import org.hsweb.web.service.GenericService;

/**
 * @author zhouhao
 */
public interface SimpleDeleteService<Pk> extends DeleteService<Pk> {
    DeleteMapper getDeleteMapper();

    default int delete(Pk pk) {
        return createDelete().where(GenericPo.Property.id, pk).exec();
    }

    /**
     * 创建dsl删除操作对象
     *
     * @return {@link Delete}
     * @see Delete
     * @see GenericService#createDelete(DeleteMapper)
     */
    default Delete<DeleteParam> createDelete() {
        return DeleteService.createDelete(getDeleteMapper());
    }

}
