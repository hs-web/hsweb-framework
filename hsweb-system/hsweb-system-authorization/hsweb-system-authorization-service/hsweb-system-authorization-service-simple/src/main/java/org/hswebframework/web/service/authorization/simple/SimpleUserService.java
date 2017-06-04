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
import org.hswebframework.web.service.authorization.*;
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

import static org.hswebframework.web.service.authorization.simple.CacheConstants.USER_AUTH_CACHE_NAME;
import static org.hswebframework.web.service.authorization.simple.CacheConstants.USER_CACHE_NAME;

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

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder = (password, salt) -> DigestUtils.md5Hex(String.format("hsweb.%s.framework.%s", password, salt));

    @Autowired(required = false)
    private DataAccessFactory dataAccessFactory;

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
        return passwordEncoder.encode(password, salt);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity selectByUsername(String username) {
        tryValidateProperty(StringUtils.hasLength(username), UserEntity.username, "id:{not_be_null}");

        Assert.notNull(username, "username:{not_be_null}");
        return createQuery().where("username", username).single();
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity selectByPk(String id) {
        tryValidateProperty(StringUtils.hasLength(id), UserEntity.id, "id:{not_be_null}");
        return createQuery().where(UserEntity.id, id).single();
    }

    @Override
    public List<UserEntity> selectByPk(List<String> id) {
        tryValidateProperty(id != null && !id.isEmpty(), UserEntity.id, "id:{not_be_null}");
        return createQuery().where().in(UserEntity.id, id).listNoPaging();
    }

    @Override
    @CacheEvict(value = USER_CACHE_NAME, key = "#userEntity.id")
    public String insert(UserEntity userEntity) {
        //用户名合法性验证
        tryValidateProperty(usernameValidator, UserEntity.username, userEntity.getUsername());
        //判断用户是否已经存在
        tryValidateProperty(null == selectByUsername(userEntity.getUsername()), UserEntity.username, "{username_exists}");
        //密码强度验证
        tryValidateProperty(passwordStrengthValidator, UserEntity.password, userEntity.getPassword());
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
                .is(UserEntity.username, userEntity.getUsername())
                .and().not(GenericEntity.id, userId)
                .total() > 0;
        tryValidateProperty(!userExists, GenericEntity.id, "{username_exists}");
        List<String> updateProperties = new ArrayList<>(Collections.singletonList("name"));
        //修改密码
        if (StringUtils.hasLength(userEntity.getPassword())) {
            //密码强度验证
            tryValidateProperty(usernameValidator, UserEntity.password, userEntity.getPassword());
            //密码MD5
            userEntity.setPassword(encodePassword(userEntity.getPassword(), userEntity.getSalt()));
            updateProperties.add(UserEntity.password);
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
                .set(UserEntity.enabled, true)
                .where(GenericEntity.id, userId)
                .exec() > 0;
    }

    @Override
    public boolean disable(String userId) {
        return DefaultDSLUpdateService.createUpdate(getDao())
                .set(UserEntity.enabled, false)
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
                .set(UserEntity.password, newPassword)
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
            return SimpleAuthenticationBuilder.build(userEntity, new ArrayList<>(), new ArrayList<>(), dataAccessFactory);
        }
        List<String> roleIdList = roleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());

        List<RoleEntity> roleEntityList = DefaultDSLQueryService.createQuery(roleDao).where().in(GenericEntity.id, roleIdList).noPaging().list();
        //权限角色关联信息
        List<PermissionRoleEntity> permissionRoleEntities = permissionRoleDao.selectByRoleIdList(roleIdList);
        return SimpleAuthenticationBuilder.build(userEntity, roleEntityList, permissionRoleEntities, dataAccessFactory);
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
        return SimpleAuthenticationBuilder.build(userEntity, roleEntityList, permissionRoleEntities, dataAccessFactory);
    }


    @Override
    public UserDao getDao() {
        return userDao;
    }
}
