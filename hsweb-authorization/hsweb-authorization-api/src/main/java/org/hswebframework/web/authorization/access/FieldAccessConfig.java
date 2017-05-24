package org.hswebframework.web.authorization.access;

import java.io.Serializable;
import java.util.Set;

/**
 * 字段级别权限控制配置,表示此用户不能对字段{@link this#getField()} 执行 {@link this#getActions()}操作
 *
 * @author zhouhao
 * @see FieldAccessController
 */
public interface FieldAccessConfig extends Serializable {

    /**
     * @return 要控制的字段名称, 字段名称支持嵌套如: user.info.name
     */
    String getField();

    /**
     * @return 对此字段的操作权限
     * @see org.hswebframework.web.authorization.Permission#ACTION_QUERY
     * @see org.hswebframework.web.authorization.Permission#ACTION_UPDATE
     */
    Set<String> getActions();

    default Type getType() {
        return Type.DENY;
    }

    enum Type {
        //目前仅支持 deny
        DENY
    }
}
