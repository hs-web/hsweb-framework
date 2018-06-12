package org.hswebframework.web.commons.entity.param;

import org.hswebframework.ezorm.core.param.Param;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.QueryEntity;

/**
 * 查询参数实体,使用<a href="https://github.com/hs-web/hsweb-easy-orm">easyorm</a>进行动态查询参数构建<br>
 * 可通过静态方法创建:<br>
 * {@link DeleteParamEntity#build()}<br>
 *
 * @author zhouhao
 * @see Param
 * @see Entity
 * @since 3.0
 */
public class DeleteParamEntity extends Param implements QueryEntity {
    private static final long serialVersionUID = 6120598637420234301L;

    /**
     * 创建一个无条件的删除条件实体
     * 创建后需自行指定条件({@link DeleteParamEntity#where(String, Object)})
     * 否则可能无法执行更新(dao实现应该禁止无条件的删除)
     *
     * @return DeleteParamEntity
     */
    public static DeleteParamEntity build() {
        return new DeleteParamEntity();
    }

    @Override
    public String toString() {
        return toHttpQueryParamString();
    }

}
