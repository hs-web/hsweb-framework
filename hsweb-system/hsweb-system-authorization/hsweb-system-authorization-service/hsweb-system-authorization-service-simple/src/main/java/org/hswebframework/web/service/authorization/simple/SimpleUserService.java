package org.hswebframework.web.service.authorization.simple;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.dao.authorization.*;
import org.hswebframework.web.entity.authorization.*;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractService;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.authorization.DataAccessFactory;
import org.hswebframework.web.service.authorization.PasswordStrengthValidator;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.authorization.UsernameValidator;
import org.hswebframework.web.validate.ValidationException;
import org.hswebframwork.utils.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hswebframework.web.service.authorization.simple.CacheConstants.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Transactional(rollbackFor = Throwable.class)
@Service("userService")
public class SimpleUserService extends AbstractService<UserEntity, String>
        implements DefaultDSLQueryService<UserEntity, String>, UserService {

    @Autowired(required = false)
    private PasswordStrengthValidator passwordStrengthValidator;

    @Autowired(required = false)
    private UsernameValidator usernameValidator;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private PermissionRoleDao permissionRoleDao;

    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired(required = false)
    private DataAccessFactory dataAccessFactory;

    @Override
    @Cacheable(value = USER_AUTH_CACHE_NAME, key = "#userId")
    public Authentication getByUserId(String userId) {
        return initUserAuthorization(userId);
    }

    @Override
    @CachePut(value = USER_AUTH_CACHE_NAME, key = "#authentication.user.id")
    public Authentication sync(Authentication authentication) {
        return authentication;
    }

    @Override
    public String encodePassword(String password, String salt) {
        return DigestUtils.md5Hex(String.format("hsweb.%s.framework.%s", password, salt));
    }

    @Override
    public void updateLoginInfo(String userId, String ip, Long loginTime) {
        Assert.notNull(userId, "userId:{not_be_null}");
        Assert.notNull(ip, "ip:{not_be_null}");
        Assert.notNull(loginTime, "loginTime:{not_be_null}");
        DefaultDSLUpdateService.createUpdate(getDao())
                .set("lastLoginIp", ip)
                .set("lastLoginTime", loginTime)
                .where(GenericEntity.id, userId)
                .exec();
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity selectByUsername(String username) {
        Assert.notNull(username, "username:{not_be_null}");
        return createQuery().where("username", username).single();
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity selectByPk(String id) {
        Assert.notNull(id, "id:{not_be_null}");
        return createQuery().where(GenericEntity.id, id).single();
    }

    @Override
    @CacheEvict(value = USER_CACHE_NAME, key = "#userEntity.id")
    public String insert(UserEntity userEntity) {
        //判断用户是否已经存在
        tryValidateProperty(null == selectByUsername(userEntity.getUsername()), "username", "{username_exists}");
        //用户名合法性验证
        tryValidateProperty(usernameValidator, "username", userEntity.getUsername());
        //密码强度验证
        tryValidateProperty(passwordStrengthValidator, "password", userEntity.getPassword());
        userEntity.setCreateTime(System.currentTimeMillis());
        userEntity.setId(IDGenerator.MD5.generate());
        userEntity.setSalt(IDGenerator.RANDOM.generate());
        userEntity.setEnabled(true);
        //验证其他属性
        tryValidate(userEntity);
        //密码MD5
        userEntity.setPassword(encodePassword(userEntity.getPassword(), userEntity.getSalt()));
        //创建用户
        userDao.insert(userEntity);
        if (userEntity instanceof BindRoleUserEntity) {
            BindRoleUserEntity bindRoleUserEntity = ((BindRoleUserEntity) userEntity);
            //插入权限信息
            if (!ListUtils.isNullOrEmpty(bindRoleUserEntity.getRoles())) {
                trySyncUserRole(userEntity.getId(), bindRoleUserEntity.getRoles());
            }
        }
        return userEntity.getId();
    }

    protected void trySyncUserRole(final String userId, final List<String> roleIdList) {
        new HashSet<>(roleIdList).stream()
                .map(roleId -> {
                    UserRoleEntity roleEntity = entityFactory.newInstance(UserRoleEntity.class);
                    roleEntity.setRoleId(roleId);
                    roleEntity.setUserId(userId);
                    return roleEntity;
                })
                .forEach(userRoleDao::insert);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER_CACHE_NAME, key = "#userId"),
            @CacheEvict(value = USER_AUTH_CACHE_NAME, key = "#userId"),
    })
    public void update(String userId, UserEntity userEntity) {
        userEntity.setId(userId);
        //判断用户是否存在
        boolean userExists = createQuery().where()
                .is("username", userEntity.getUsername())
                .and().not(GenericEntity.id, userId)
                .total() > 0;
        tryValidateProperty(!userExists, GenericEntity.id, "{username_exists}");
        List<String> updateProperties = Arrays.asList("name");
        //修改密码
        if (!StringUtils.hasLength(userEntity.getPassword())) {
            //密码强度验证
            tryValidateProperty(usernameValidator, "password", userEntity.getPassword());
            //密码MD5
            userEntity.setPassword(encodePassword(userEntity.getPassword(), userEntity.getSalt()));
            updateProperties.add("password");
        }
        //修改数据
        DefaultDSLUpdateService.createUpdate(getDao(), userEntity)
                .includes(updateProperties.toArray(new String[updateProperties.size()]))
                .where(GenericEntity.id, userEntity.getId())
                .exec();
        if (userEntity instanceof BindRoleUserEntity) {
            BindRoleUserEntity bindRoleUserEntity = ((BindRoleUserEntity) userEntity);
            //删除旧的数据
            userRoleDao.deleteByUserId(bindRoleUserEntity.getId());
            //同步角色信息
            trySyncUserRole(userEntity.getId(), bindRoleUserEntity.getRoles());
        }
    }

    @Override
    public boolean enable(String userId) {
        return DefaultDSLUpdateService.createUpdate(getDao())
                .set("enabled", true)
                .where(GenericEntity.id, userId)
                .exec() > 0;
    }

    @Override
    public boolean disable(String userId) {
        return DefaultDSLUpdateService.createUpdate(getDao())
                .set("enabled", false)
                .where(GenericEntity.id, userId)
                .exec() > 0;
    }

    @Override
    public void updatePassword(String userId, String oldPassword, String newPassword) {
        UserEntity userEntity = selectByPk(userId);
        assertNotNull(userEntity);
        oldPassword = encodePassword(oldPassword, userEntity.getSalt());
        if (!userEntity.getPassword().equals(oldPassword)) {
            throw new ValidationException("{old_password_error}", "password");
        }
        newPassword = encodePassword(newPassword, userEntity.getSalt());
        DefaultDSLUpdateService.createUpdate(getDao())
                .set("password", newPassword)
                .where(GenericEntity.id, userId)
                .exec();
    }

    @Override
    public Authentication initUserAuthorization(String userId) {
        UserEntity userEntity = selectByPk(userId);
        assertNotNull(userEntity);
        //用户持有的角色
        List<UserRoleEntity> roleEntities = userRoleDao.selectByUserId(userId);
        if (ListUtils.isNullOrEmpty(roleEntities)) {
            return new SimpleAuthentication(userEntity, new ArrayList<>(), new ArrayList<>(), dataAccessFactory);
        }
        List<String> roleIdList = roleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());

        List<RoleEntity> roleEntityList = DefaultDSLQueryService.createQuery(roleDao).where().in(GenericEntity.id, roleIdList).noPaging().list();
        //权限角色关联信息
        List<PermissionRoleEntity> permissionRoleEntities = permissionRoleDao.selectByRoleIdList(roleIdList);
        return new SimpleAuthentication(userEntity, roleEntityList, permissionRoleEntities, dataAccessFactory);
    }

    @Override
    public Authentication initAdminAuthorization(String userId) {
        UserEntity userEntity = selectByPk(userId);
        assertNotNull(userEntity);
        //所有权限信息
        List<PermissionEntity> permissionEntities = DefaultDSLQueryService
                .createQuery(permissionDao).noPaging().list();
        List<PermissionRoleEntity> permissionRoleEntities = permissionEntities
                .stream().map(permission -> {
                    PermissionRoleEntity entity = entityFactoryIsEnabled()
                            ? entityFactory.newInstance(PermissionRoleEntity.class)
                            : new SimplePermissionRoleEntity();
                    entity.setRoleId("admin");  //always admin
                    entity.setPermissionId(permission.getId());
                    entity.setActions(permission.getActions()
                            .stream()
                            .map(ActionEntity::getAction)
                            .collect(Collectors.toList()));
                    return entity;
                }).collect(Collectors.toList());
        List<RoleEntity> roleEntityList = DefaultDSLQueryService.createQuery(roleDao).noPaging().list();
        if (roleEntityList.isEmpty()) {
            RoleEntity admin = entityFactory.newInstance(RoleEntity.class);
            admin.setId("admin");
            admin.setName("admin");
            roleEntityList.add(admin);
        }
        return new SimpleAuthentication(userEntity, roleEntityList, permissionRoleEntities, dataAccessFactory);
    }


    @Override
    public UserDao getDao() {
        return userDao;
    }
}
