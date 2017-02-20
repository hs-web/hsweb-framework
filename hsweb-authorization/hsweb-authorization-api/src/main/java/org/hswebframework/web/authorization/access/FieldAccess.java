package org.hswebframework.web.authorization.access;

import java.io.Serializable;
import java.util.Set;

/**
 * 字段级别权限控制配置,表示此用户不能对字段{@link this#getField()} 执行 {@link this#getActions()}操作
 *
 * @author zhouhao
 * @see FieldAccessController
 */
public interface FieldAccess extends Serializable {
    /**
     * 获取字段名称,字段名称支持嵌套如: user.info.name
     * 此值为不能操作的字段
     *
     * @return 字段名称
     */
    String getField();

    /**
     * 对此字段的操作权限
     *
     * @return 操作权限集合
     */
    Set<String> getActions();
}
