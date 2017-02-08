package org.hswebframework.web.authorization.access;


import org.hswebframework.web.authorization.Permission;

import java.io.Serializable;

/**
 * 数据级的权限控制
 *
 * @author zhouhao
 * @see org.hswebframework.web.authorization.access.CustomDataAccess
 * @see org.hswebframework.web.authorization.access.OwnCreatedDataAccess
 * @see org.hswebframework.web.authorization.access.ScriptDataAccess
 */
public interface DataAccess extends Serializable {

    /**
     * 对数据的操作事件
     *
     * @return 操作时间
     * @see Permission#ACTION_ADD
     * @see Permission#ACTION_DELETE
     * @see Permission#ACTION_GET
     * @see Permission#ACTION_QUERY
     * @see Permission#ACTION_UPDATE
     */
    String getAction();

    enum Type {
        OWN_CREATED("自己创建的数据"),
        SCRIPT("脚本"),
        CUSTOM("自定义控制器");

        public final String text;

        Type(String text) {
            this.text = text;
        }
    }
}
