package org.hswebframework.web.commons.entity.param;

import org.hswebframework.ezorm.core.param.UpdateParam;
import org.hswebframework.web.commons.entity.Entity;

/**
 * 修改参数实体,使用<a href="https://github.com/hs-web/hsweb-easy-orm">easyorm</a>进行动态参数构建
 *
 * @author zhouhao
 * @see UpdateParam
 * @see Entity
 * @since 3.0
 */
public class UpdateParamEntity<T> extends UpdateParam<T> implements Entity {
    public UpdateParamEntity() {
    }

    public UpdateParamEntity(T data) {
        super(data);
    }

    /**
     * 创建一个无任何条件并指定数据的更新参数实体
     * 创建后需自行指定条件({@link UpdateParamEntity#where(String, Object)})
     * 否则可能无法执行更新(dao实现应该禁止无条件的更新)
     *
     * @param data 要更新的数据
     * @param <T>  数据泛型
     * @return 更新参数实体
     */
    public static <T> UpdateParamEntity<T> build(T data) {
        return new UpdateParamEntity<>(data);
    }

    /**
     * 创建一个单个条件并指定数据的更新参数实体,条件默认为is:
     * <br>例如:<br>
     * <code>
     * // where id = #{id}
     * <br>
     * UpdateParamBean.build(data,"id",id);
     * </code>
     *
     * @param data  要更新的数据
     * @param field 条件名
     * @param value 条件值
     * @param <T>   数据泛型
     * @return 更新参数实体
     */
    public static <T> UpdateParamEntity<T> build(T data, String field, Object value) {
        return new UpdateParamEntity<>(data).where(field, value);
    }
}
