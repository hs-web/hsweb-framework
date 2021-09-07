package org.hswebframework.web.authorization.dimension;

import reactor.core.publisher.Flux;

import java.util.Collection;

/**
 * 维度管理器
 *
 * @author zhouhao
 * @since 4.0.12
 */
public interface DimensionManager {

    /**
     * 获取用户维度
     *
     * @param type   维度类型
     * @param userId 用户ID
     * @return 用户维度信息
     */
    Flux<DimensionUserDetail> getUserDimension(Collection<String> userId);


}
