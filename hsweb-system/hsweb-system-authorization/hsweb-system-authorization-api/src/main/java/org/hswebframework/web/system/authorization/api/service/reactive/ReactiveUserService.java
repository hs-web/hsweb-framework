package org.hswebframework.web.system.authorization.api.service.reactive;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式用户服务
 *
 * @author zhouhao
 * @since 4.0.0
 */
public interface ReactiveUserService {

    /**
     * 创建一个新的用户实例
     *
     * @return 用户实例
     */
    Mono<UserEntity> newUserInstance();

    /**
     * 保存用户
     *
     * @param userEntity 用户实体
     * @return 是否成功
     */
    Mono<Boolean> saveUser(Mono<UserEntity> userEntity);

    /**
     * 根据用户名查询用户实体，如果用户不存在则返回{@link Mono#empty()}
     *
     * @param username 用户名
     * @return 用户实体
     */
    Mono<UserEntity> findByUsername(String username);

    /**
     * 根据用户名查询用户实体，如果用户不存在则返回{@link Mono#empty()}
     *
     * @param id 用户名
     * @return 用户实体
     */
    Mono<UserEntity> findById(String id);

    /**
     * 根据用户名和密码查询用户实体，如果用户不存在或者密码不匹配则返回{@link Mono#empty()}
     *
     * @param username      用户名
     * @param plainPassword 明文密码
     * @return 用户实体
     */
    Mono<UserEntity> findByUsernameAndPassword(String username, String plainPassword);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param state  状态
     * @return 修改数量
     */
    Mono<Integer> changeState(Publisher<String> userId, byte state);

    /**
     * 修改用户密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    Mono<Boolean> changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 根据查询条件查询用户
     * @param queryParam 动态查询条件
     * @return 用户列表
     */
    Flux<UserEntity> findUser(QueryParam queryParam);

    /**
     * 根据查询条件查询用户数量
     * @param queryParam 查询条件
     * @return 用户数量
     */
    Mono<Integer> countUser(QueryParam queryParam);

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 是否成功
     * @see org.hswebframework.web.system.authorization.api.event.UserDeletedEvent
     */
    Mono<Boolean> deleteUser(String userId);

}
