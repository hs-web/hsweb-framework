package org.hswebframework.web.authorization.token;

import org.hswebframework.web.authorization.Authentication;
import reactor.core.publisher.Mono;

/**
 * 响应式令牌认证提供者。
 *
 * <p>当请求中的 {@link ParsedToken#getType()} 与提供者声明的类型一致时，由
 * {@link UserTokenReactiveAuthenticationSupplier} 调用本接口完成原始令牌的完整认证。
 * 本接口不管理 {@link UserTokenManager} 中的有状态用户令牌。</p>
 *
 * <p>实现必须保持非阻塞且可重复调用，不得记录或长期持有原始令牌。</p>
 *
 * @author zhouhao
 * @see UserTokenReactiveAuthenticationSupplier
 * @since 5.0.2
 */
public interface ReactiveTokenAuthenticationProvider {

    /**
     * 获取本提供者唯一拥有的令牌类型。
     *
     * @return 非空、大小写敏感的令牌类型；重复类型会导致初始化失败
     */
    String getTokenType();

    /**
     * 对已完成传输层提取、但尚未认证的令牌进行认证。
     *
     * <p>返回空表示认证失败，异常将原样传播。提供者一旦按类型命中，调用方不会再回退
     * {@link UserTokenManager} 或其他提供者。实现可以组合响应式存储或远程调用，但不得
     * 阻塞、在主链外订阅，或将敏感令牌写入日志和链路追踪。</p>
     *
     * @param token 非空的令牌解析结果；其类型已经与 {@link #getTokenType()} 精确匹配
     * @return 认证成功后的权限信息；空表示认证失败
     */
    Mono<Authentication> authenticate(ParsedToken token);
}
