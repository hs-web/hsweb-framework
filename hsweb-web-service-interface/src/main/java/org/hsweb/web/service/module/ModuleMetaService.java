package org.hsweb.web.service.module;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.module.ModuleMeta;
import org.hsweb.web.service.GenericService;

import java.util.List;

/**
 * Created by zhouhao on 16-5-10.
 */
public interface ModuleMetaService extends GenericService<ModuleMeta, String> {

    default List<ModuleMeta> selectByKeyAndRoleId(String key, String... roleId) throws Exception {
        QueryParam queryParam = new QueryParam();
        queryParam.where("key", key).and("role_id$IN", roleId);
        return this.select(queryParam);
    }

    default List<ModuleMeta> selectByKey(String key) throws Exception {
        QueryParam queryParam = new QueryParam();
        queryParam.where("key", key);
        return this.select(queryParam);
    }

}
