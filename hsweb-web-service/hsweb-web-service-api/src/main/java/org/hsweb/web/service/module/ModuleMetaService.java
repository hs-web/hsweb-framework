package org.hsweb.web.service.module;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.module.ModuleMeta;
import org.hsweb.web.service.GenericService;

import java.util.List;

public interface ModuleMetaService extends GenericService<ModuleMeta, String> {

    List<ModuleMeta> selectByKeyAndRoleId(String key, List<String> roleId);

    ModuleMeta selectSingleByKeyAndRoleId(String key, List<String> roleId);

    String selectMD5SingleByKeyAndRoleId(String key, List<String> roleId);

    default List<ModuleMeta> selectByKey(String key) {
        QueryParam queryParam = new QueryParam();
        queryParam.where("key", key);
        return this.select(queryParam);
    }

}
