package org.hsweb.web.dao.role;

import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.bean.po.role.RoleModule;

import java.util.List;

/**
 * 系统模块角色绑定数据映射接口
 * Created by generator
 */
public interface RoleModuleMapper extends GenericMapper<RoleModule, String> {
    /**
     * 根据角色id查询
     *
     * @param roleId 角色id
     * @return
     * @throws Exception
     */
    List<RoleModule> selectByRoleId(String roleId) throws Exception;

    int deleteByRoleId(String roleId) throws Exception;

    int deleteByModuleId(String moduleId) throws Exception;
}
