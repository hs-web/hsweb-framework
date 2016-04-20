package org.hsweb.web.service.impl.user;

import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.bean.po.role.UserRole;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.dao.role.UserRoleMapper;
import org.hsweb.web.dao.user.UserMapper;
import org.hsweb.web.exception.BusinessException;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.module.ModuleService;
import org.hsweb.web.service.user.UserService;
import org.hsweb.web.utils.RandomUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.webbuilder.utils.common.MD5;

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

    public User selectByUserName(String username) throws Exception {
        return this.getMapper().selectByUserName(username);
    }

    @Override
    public String insert(User data) throws Exception {
        tryValidPo(data);
        Assert.isNull(selectByUserName(data.getUsername()), "用户已存在!");

        data.setU_id(RandomUtil.randomChar(6));
        data.setCreate_date(new Date());
        data.setUpdate_date(new Date());
        data.setPassword(MD5.encode(data.getPassword()));
        userMapper.insert(new InsertParam<>(data));
        String id = data.getU_id();
        //添加角色关联
        if (data.getUserRoles().size() != 0) {
            for (UserRole userRole : data.getUserRoles()) {
                userRole.setU_id(RandomUtil.randomChar());
                userRole.setUser_id(data.getU_id());
                userRoleMapper.insert(new InsertParam<>(userRole));
            }
        }
        return id;
    }

    @Override
    public int update(User data) throws Exception {
        tryValidPo(data);
        User old = this.selectByUserName(data.getUsername());
        if (old != null && !old.getU_id().equals(data.getU_id())) {
            throw new BusinessException("用户名已存在!");
        }
        data.setUpdate_date(new Date());
        if (!"$default".equals(data.getPassword())) {
            data.setPassword(MD5.encode(data.getPassword()));
            userMapper.updatePassword(data);
        }
        int i = userMapper.update(new UpdateParam<>(data));
        if (data.getUserRoles().size() != 0) {
            //删除所有
            userRoleMapper.deleteByUserId(data.getU_id());
            for (UserRole userRole : data.getUserRoles()) {
                userRole.setU_id(RandomUtil.randomChar());
                userRole.setUser_id(data.getU_id());
                userRoleMapper.insert(new InsertParam<>(userRole));
            }
        }
        return i;
    }

    @Override
    public void initAdminUser(User user) throws Exception {
        QueryParam queryParam = new QueryParam();
        queryParam.orderBy("sort_index");
        List<Module> modules = moduleService.select(queryParam);
        Map<Module, Set<String>> roleInfo = new LinkedHashMap<>();
        for (Module module : modules) {
            roleInfo.put(module, new LinkedHashSet<>(module.getM_optionMap().keySet()));
        }
        user.setRoleInfo(roleInfo);
    }

    @Override
    public void initGuestUser(User user) throws Exception {
        List<UserRole> userRoles = userRoleMapper.select(new QueryParam().where("role_id", "guest"));
        user.setUserRoles(userRoles);
        user.initRoleInfo();
    }
}
