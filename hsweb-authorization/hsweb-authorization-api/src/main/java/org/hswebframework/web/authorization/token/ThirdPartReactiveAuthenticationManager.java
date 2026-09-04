package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.Authentication;
import reactor.core.publisher.Mono;

/**
 * 已登记用户令牌的第三方权限加载器。
 *
 * <p>{@link UserTokenReactiveAuthenticationSupplier} 仅在 {@link UserTokenManager} 完成
 * 有状态令牌校验后，根据令牌记录中的用户和类型调用本接口。需要直接验证请求原始令牌的
 * 扩展应实现 {@link ReactiveTokenAuthenticationProvider}。</p>
 *
 * @author zhouhao
 * @see ReactiveTokenAuthenticationProvider
 * @since 1.0
 */
public interface ThirdPartReactiveAuthenticationManager {

    /**
     * 获取对应的已登记用户令牌类型。
     *
     * @return 与 {@link UserToken#getType()} 对应的令牌类型
     */
    String getTokenType();

    /**
     * 根据已通过令牌校验的用户 ID 获取权限信息。
     *
     * @param userId 用户ID
     * @return 权限信息；空表示未找到，异常将向调用方传播
     */
    Mono<Authentication> getByUserId(String userId);

}
