package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.service.CreateEntityService;
import org.hswebframework.web.service.InsertService;
import org.hswebframework.web.service.QueryByEntityService;
import org.hswebframework.web.service.QueryService;
import org.hswebframework.web.validate.ValidationException;

import java.util.List;

/**
 * 用户服务,提供对用户信息对常用操作
 *
 * @author zhouhao
 * @since 3.0
 */
public interface UserService extends
        CreateEntityService<UserEntity>,
        QueryByEntityService<UserEntity>,
        QueryService<UserEntity, String>,
        InsertService<UserEntity, String> {

    /**
     * 新增用户
     *
     * @param data 要添加的数据
     * @return 用户id
     * @see org.hswebframework.web.service.authorization.events.UserCreatedEvent
     * @see BindRoleUserEntity
     */
    @Override
    String insert(UserEntity data);

    /**
     * 启用用户
     *
     * @param userId 用户Id
     * @return 是否启用成功
     * @see UserEntity#setStatus(Byte)
     * @see org.hswebframework.web.commons.entity.DataStatus#STATUS_ENABLED
     */
    boolean enable(String userId);

    /**
     * 禁用用户
     *
     * @param userId 用户Id
     * @return 是否启用成功
     * @see UserEntity#setStatus(Byte)
     * @see org.hswebframework.web.commons.entity.DataStatus#STATUS_DISABLED
     */
    boolean disable(String userId);

    /**
     * 修改用户信息,如果传入对实体实现了{@link BindRoleUserEntity},将更新用户的权限信息,更新逻辑:<br>
     * 删除用户的权限信息,将新的权限信息重新insert,⚠️注意: 如果{@link BindRoleUserEntity#getRoles()}等于<code>null</code>,将不更新角色信息.<br>
     * 用户信息更新后,将发布事件:{@link org.hswebframework.web.service.authorization.events.UserModifiedEvent},在其他服务可通过监听此事件来
     * 来实现特定的操作,如清空用户权限缓存等.<br>
     *
     * @param userId   用户ID
     * @param userBean 用户信息实体类
     * @see org.hswebframework.web.service.authorization.events.UserModifiedEvent
     * @see org.springframework.context.ApplicationListener
     * @see org.springframework.context.event.EventListener
     * @see BindRoleUserEntity
     */
    void update(String userId, UserEntity userBean);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名,区分大小写,不能为空
     * @return 用户信息, 如果不存在则返回 <code>null</code>
     */
    UserEntity selectByUsername(String username);

    /**
     * 根据用户名和密码查询用户信息，在验证用户名密码是否正确是可以使用此方法
     *
     * @param plainUsername 用户名,区分大小写,不能为空
     * @param plainPassword 明文密码,区分大小写,不能为空
     * @return 用户信息, 如果用户名或者密码错误, 则返回<code>null</code>
     * @see PasswordEncoder
     */
    UserEntity selectByUserNameAndPassword(String plainUsername, String plainPassword);

    /**
     * 对密码进行加密混淆
     *
     * @param password 明文密码,不能为空
     * @param salt     混淆盐,不能为空
     * @return 加密后对密码
     * @see PasswordEncoder
     */
    String encodePassword(String password, String salt);

    /**
     * 修改用户密码
     *
     * @param userId      用户ID,不能为空
     * @param oldPassword 旧的明文密码,不能为空
     * @param newPassword 新的明文密码,不能为空
     * @throws ValidationException 旧密码错误时抛出此异常
     * @see PasswordEncoder
     * @see org.hswebframework.web.service.authorization.events.UserModifiedEvent
     * @see org.springframework.context.ApplicationListener
     * @see java.util.EventListener
     */
    void updatePassword(String userId, String oldPassword, String newPassword) throws ValidationException;

    /**
     * 获取用户的全部角色信息
     *
     * @param userId 用户ID,不能为空
     * @return 永远不为null, 如果用户不存在或者用户没有任何角色, 返回空集合.
     */
    List<RoleEntity> getUserRole(String userId);

    /**
     * 根据角色id集合获取对应的全部用户
     *
     * @param roleIdList 角色ID集合
     * @return 用户, 不存在时返回空集合,不会返回null
     */
    List<UserEntity> selectByUserByRole(List<String> roleIdList);
}
