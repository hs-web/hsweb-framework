package org.hsweb.web.service.impl.role;

import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.po.role.Role;
import org.hsweb.web.bean.po.role.RoleModule;
import org.hsweb.web.dao.role.RoleMapper;
import org.hsweb.web.dao.role.RoleModuleMapper;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.module.ModuleService;
import org.hsweb.web.service.role.RoleService;
import org.hsweb.web.core.utils.RandomUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 后台管理角色服务类
 * Created by zh.sqy@qq.com
 */
@Service("roleService")
public class RoleServiceImpl extends AbstractServiceImpl<Role, String> implements RoleService {

    //默认数据映射接口
    @Resource
    protected RoleMapper roleMapper;

    @Resource
    protected RoleModuleMapper roleModuleMapper;

    @Resource
    protected ModuleService moduleService;

    @Override
    protected RoleMapper getMapper() {
        return this.roleMapper;
    }

    @Override
    public String insert(Role data) {
        String id = super.insert(data);
        List<RoleModule> roleModule = data.getModules();
        if (roleModule != null && roleModule.size() > 0) {
            //保存角色模块关联
            for (RoleModule module : roleModule) {
                module.setId(RandomUtil.randomChar(6));
                module.setRoleId(data.getId());
                roleModuleMapper.insert(new InsertParam<>(module));
            }
        }
        return id;
    }

    @Override
    public int update(Role data){
        int l = super.update(data);
        List<RoleModule> roleModule = data.getModules();
        if (roleModule != null && roleModule.size() > 0) {
            //先删除所有roleModule
            roleModuleMapper.deleteByRoleId(data.getId());
            //保存角色模块关联
            for (RoleModule module : roleModule) {
                module.setId(RandomUtil.randomChar(6));
                module.setRoleId(data.getId());
                roleModuleMapper.insert(new InsertParam<>(module));
            }
        }
        return l;
    }
}
