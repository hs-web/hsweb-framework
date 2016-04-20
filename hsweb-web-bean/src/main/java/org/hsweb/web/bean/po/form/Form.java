package org.hsweb.web.bean.po.form;

import org.hsweb.web.bean.po.GenericPo;

/**
 * 自定义表单
 * Created by generator
 */
public class Form extends GenericPo<String> {

    private static final long serialVersionUID = 8910856253780046561L;

    //主键
    private String u_id;

    //名称
    private String name;

    //表单
    private String html;

    //表结构定义(json)
    private String meta;

    //表单配置
    private String config;

    //备注
    private String remark;

    //表单版本
    private int version;

    //是否正在使用
    private boolean using;

    //创建时间
    private java.util.Date create_date;

    //最后一次修改时间
    private java.util.Date update_date;

    /**
     * 获取 主键
     *
     * @return String 主键
     */
    public String getU_id() {
        if (this.u_id == null)
            return "";
        return this.u_id;
    }

    /**
     * 设置 主键
     */
    public void setU_id(String u_id) {
        this.u_id = u_id;
    }

    /**
     * 获取 名称
     *
     * @return String 名称
     */
    public String getName() {
        if (this.name == null)
            return "";
        return this.name;
    }

    /**
     * 设置 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 备注
     *
     * @return String 备注
     */
    public String getRemark() {
        if (this.remark == null)
            return "";
        return this.remark;
    }

    /**
     * 设置 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }


    /**
     * 获取 创建时间
     *
     * @return java.util.Date 创建时间
     */
    public java.util.Date getCreate_date() {
        return this.create_date;
    }

    /**
     * 设置 创建时间
     */
    public void setCreate_date(java.util.Date create_date) {
        this.create_date = create_date;
    }

    /**
     * 获取 最后一次修改时间
     *
     * @return java.util.Date 最后一次修改时间
     */
    public java.util.Date getUpdate_date() {
        return this.update_date;
    }

    /**
     * 设置 最后一次修改时间
     */
    public void setUpdate_date(java.util.Date update_date) {
        this.update_date = update_date;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }
}
