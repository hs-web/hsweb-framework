package org.hsweb.web.service.impl.user;

import org.hsweb.commons.MD5;
import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.bean.po.role.UserRole;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.bean.po.user.User.Property;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.utils.RandomUtil;
import org.hsweb.web.dao.role.UserRoleMapper;
import org.hsweb.web.dao.user.UserMapper;
import org.hsweb.web.service.GenericService;
import org.hsweb.web.service.QueryService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.module.ModuleService;
import org.hsweb.web.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * 后台管理用户服务类
 * Created by generator
 */
@Service("userService")
public class UserServiceImpl extends AbstractServiceImpl<User, String> implements UserService {

    //默认数据映射接口
    @Resource
    protected UserMapper userMapper;

    @Resource
    protected UserRoleMapper userRoleMapper;

    @Resource
    protected ModuleService moduleService;

    @Override
    protected UserMapper getMapper() {
        return this.userMapper;
    }

    public User selectByUserName(String username) {
        return this.getMapper().selectByUserName(username);
    }

    @Override
    public String insert(User data) {
        tryValidPo(data);
        Assert.isNull(selectByUserName(data.getUsername()), "用户已存在!");
        data.setId(RandomUtil.randomChar(6));
        data.setCreateDate(new Date());
        data.setUpdateDate(new Date());
        data.setPassword(MD5.encode(data.getPassword()));
        data.setStatus(1);
        userMapper.insert(new InsertParam<>(data));
        String id = data.getId();
        //添加角色关联
        if (data.getUserRoles() != null) {
            for (UserRole userRole : data.getUserRoles()) {
                userRole.setId(RandomUtil.randomChar());
                userRole.setUserId(data.getId());
                userRoleMapper.insert(new InsertParam<>(userRole));
            }
        }
        return id;
    }

    @Override
    public int update(User data) {
        tryValidPo(data);
        User old = this.selectByUserName(data.getUsername());
        if (old != null && !old.getId().equals(data.getId())) {
            throw new BusinessException("用户名已存在!");
        }
        data.setUpdateDate(new Date());
        if (!"$default".equals(data.getPassword())) {
            data.setPassword(MD5.encode(data.getPassword()));
            userMapper.updatePassword(data);
        }
        int i = createUpdate(data).excludes(Property.status, Property.password, Property.createDate).fromBean().where(Property.id).exec();
        if (data.getUserRoles() != null) {
            //删除所有
            userRoleMapper.deleteByUserId(data.getId());
            for (UserRole userRole : data.getUserRoles()) {
                userRole.setId(RandomUtil.randomChar());
                userRole.setUserId(data.getId());
                userRoleMapper.insert(new InsertParam<>(userRole));
            }
        }
        return i;
    }

    @Override
    public void initAdminUser(User user) {
        List<Module> modules = moduleService.createQuery().orderByAsc(Module.Property.sortIndex).listNoPaging();
        Map<Module, Set<String>> roleInfo = new LinkedHashMap<>();
        for (Module module : modules) {
            roleInfo.put(module, new LinkedHashSet<>(module.getOptionalMap().keySet()));
        }
        user.setRoleInfo(roleInfo);
    }

    @Override
    public void initGuestUser(User user) {
        List<UserRole> userRoles = QueryService.createQuery(userRoleMapper).where(UserRole.Property.roleId, "guest").list();
        user.setUserRoles(userRoles);
        user.initRoleInfo();
    }

    @Override
    public void enableUser(String id) {
        User user = selectByPk(id);
        if (user == null) throw new NotFoundException("用户不存在!");
        user.setStatus(1);
        createUpdate(user).includes(Property.status).where(Property.id, id).exec();
    }

    @Override
    public void disableUser(String id) {
        User user = selectByPk(id);
        if (user == null) throw new NotFoundException("用户不存在!");
        user.setStatus(-1);
        createUpdate(user).includes(Property.status).where(Property.id, id).exec();
    }

    @Override
    public int delete(String s) {
        throw new BusinessException("服务不支持", 500);
    }


}
