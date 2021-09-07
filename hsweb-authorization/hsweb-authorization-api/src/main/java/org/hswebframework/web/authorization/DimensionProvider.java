package org.hswebframework.web.authorization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * 维度提供商,用户管理维度信息
 *
 * @author zhouhao
 * @since 4.0
 */
public interface DimensionProvider {

    /**
     * 获取全部支持的维度
     *
     * @return 全部支持的维度
     */
    Flux<? extends DimensionType> getAllType();

    /**
     * 获取用户获取维度信息
     *
     * @param userId 用户ID
     * @return 维度列表
     */
    Flux<? extends Dimension> getDimensionByUserId(String userId);

    /**
     * 根据维度类型和ID获取维度信息
     *
     * @param type 类型
     * @param id   ID
     * @return 维度信息
     */
    Mono<? extends Dimension> getDimensionById(DimensionType type, String id);

    /**
     * 根据维度类型和Id获取多个维度
     * @param type 类型
     * @param idList ID
     * @return 维度信息
     */
    default Flux<? extends Dimension> getDimensionsById(DimensionType type, Collection<String> idList){
        return Flux
                .fromIterable(idList)
                .flatMap(id->this.getDimensionById(type,id));
    }

    /**
     * 根据维度ID获取用户ID
     *
     * @param dimensionId 维度ID
     * @return 用户ID
     */
    Flux<String> getUserIdByDimensionId(String dimensionId);
}
