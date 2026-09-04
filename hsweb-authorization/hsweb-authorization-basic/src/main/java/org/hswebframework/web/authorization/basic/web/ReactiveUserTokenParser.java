package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.ParsedToken;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 从响应式 HTTP 请求中提取令牌。
 *
 * <p>解析器由 {@link UserTokenWebFilter} 按 Spring Order 顺序调用。实现应只进行无阻塞、
 * 低成本的令牌提取和类型识别，完整认证由后续认证组件负责。</p>
 *
 * @author zhouhao
 * @see Ordered
 * @see UserTokenWebFilter
 */
public interface ReactiveUserTokenParser {

    /**
     * 尝试解析请求中的令牌。
     *
     * @param exchange 当前请求，不可为空
     * @return 解析结果；空表示不识别该请求并继续下一解析器，异常会终止解析且不回退
     */
    Mono<ParsedToken> parseToken(ServerWebExchange exchange);
}
