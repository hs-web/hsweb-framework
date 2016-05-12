package org.hsweb.web.bean.po.module;

import org.hsweb.web.bean.po.GenericPo;

/**
 * 模块配置信息
 * Created by zhouhao on 16-5-10.
 */
public class ModuleMeta extends GenericPo<String> {

    private String key;

    private String remark;

    private String module_id;

    private String role_id;

    private String meta;

    private int status;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getModule_id() {
        return module_id;
    }

    public void setModule_id(String module_id) {
        this.module_id = module_id;
    }

    public String getRole_id() {
        return role_id;
    }

    public void setRole_id(String role_id) {
        this.role_id = role_id;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
}
