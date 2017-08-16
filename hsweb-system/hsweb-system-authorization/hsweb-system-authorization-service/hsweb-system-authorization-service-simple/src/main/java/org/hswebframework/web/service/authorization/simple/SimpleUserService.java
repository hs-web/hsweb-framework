package org.hswebframework.web.service.authorization.simple;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.dao.authorization.RoleDao;
import org.hswebframework.web.dao.authorization.UserDao;
import org.hswebframework.web.dao.authorization.UserRoleDao;
import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.authorization.UserRoleEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractService;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.authorization.*;
import org.hswebframework.web.validate.ValidationException;
import org.hswebframework.utils.ListUtils;
import org.hswebframework.web.validator.group.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hswebframework.web.service.authorization.simple.CacheConstants.USER_AUTH_CACHE_NAME;
import static org.hswebframework.web.service.authorization.simple.CacheConstants.USER_CACHE_NAME;

/**
 * 默认的用户服务实现
 *
 * @author zhouhao
 * @since 3.0
 */
@Transactional(rollbackFor = Throwable.class)
@Service("userService")
public class SimpleUserService extends AbstractService<UserEntity, String>
        implements DefaultDSLQueryService<UserEntity, String>, UserService, AuthorizationSettingTypeSupplier {

    @Autowired(required = false)
    private PasswordStrengthValidator passwordStrengthValidator;

    @Autowired(required = false)
    private UsernameValidator usernameValidator;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder = (password, salt) -> DigestUtils.md5Hex(String.format("hsweb.%s.framework.%s", password, salt));

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private RoleDao roleDao;

    @Override
    public String encodePassword(String password, String salt) {
        return passwordEncoder.encode(password, salt);
    }

    @Override
    public UserEntity createEntity() {
        return entityFactory.newInstance(BindRoleUserEntity.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity selectByUsername(String username) {
        if (null == username) return null;
        return createQuery().where("username", username).single();
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity selectByPk(String id) {
        if (null == id) return null;
        UserEntity userEntity = createQuery().where(UserEntity.id, id).single();
        if (null != userEntity) {
            List<String> roleId = userRoleDao.selectByUserId(id).stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
            BindRoleUserEntity roleUserEntity = entityFactory.newInstance(BindRoleUserEntity.class, userEntity);
            roleUserEntity.setRoles(roleId);
            return roleUserEntity;
        }
        return null;
    }

    @Override
    public List<UserEntity> selectByPk(List<String> id) {
        if (CollectionUtils.isEmpty(id)) return new ArrayList<>();
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
        userEntity.setStatus(DataStatus.STATUS_ENABLED);
        //验证其他属性
        tryValidate(userEntity, CreateGroup.class);
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
        UserEntity oldUser = selectByPk(userId);
        assertNotNull(oldUser);
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
            userEntity.setPassword(encodePassword(userEntity.getPassword(), oldUser.getSalt()));
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
                .set(UserEntity.status, DataStatus.STATUS_ENABLED)
                .where(GenericEntity.id, userId)
                .exec() > 0;
    }

    @Override
    public boolean disable(String userId) {
        return DefaultDSLUpdateService.createUpdate(getDao())
                .set(UserEntity.status, DataStatus.STATUS_DISABLED)
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
    public List<RoleEntity> getUserRole(String userId) {
        Objects.requireNonNull(userId);
        List<UserRoleEntity> roleEntities = userRoleDao.selectByUserId(userId);
        if (roleEntities.isEmpty()) return new ArrayList<>();
        List<String> roleIdList = roleEntities.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());
        return DefaultDSLQueryService
                .createQuery(roleDao).where()
                .in(GenericEntity.id, roleIdList)
                .noPaging()
                .list();
    }

    @Override
    public UserDao getDao() {
        return userDao;
    }

    @Override
    public Set<SettingInfo> get(String userId) {
        UserEntity userEntity = selectByPk(userId);
        if (null == userEntity) return new HashSet<>();
        List<UserRoleEntity> roleEntities = userRoleDao.selectByUserId(userId);
        //使用角色配置
        Set<SettingInfo> settingInfo = roleEntities.stream()
                .map(entity -> new SettingInfo(SETTING_TYPE_ROLE, entity.getRoleId()))
                .collect(Collectors.toSet());
        //使用用户的配置
        settingInfo.add(new SettingInfo(SETTING_TYPE_USER, userId));
        return settingInfo;
    }
}
