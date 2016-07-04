package org.hsweb.web.bean.po.form;

import org.hibernate.validator.constraints.Length;
import org.hsweb.web.bean.po.GenericPo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 自定义表单
 * Created by generator
 */
public class Form extends GenericPo<String> {

    private static final long serialVersionUID = 8910856253780046561L;

    //名称
    @NotNull(message = "表单名称不能为null")
    @Length(min = 4, message = "表名长度不能小于4")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "表名只能由字母开头,由字母,数字,下划线组成(字母只能为小写)")
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

    //修订版本号
    private int revision;

    //发布版版本号
    private int release;

    //是否正在使用
    private boolean using;

    //创建时间
    private java.util.Date createDate;

    //最后一次修改时间
    private java.util.Date updateDate;

    //分类ID
    private String classifiedId;

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
    public java.util.Date getCreateDate() {
        return this.createDate;
    }

    /**
     * 设置 创建时间
     */
    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }

    /**
     * 获取 最后一次修改时间
     *
     * @return java.util.Date 最后一次修改时间
     */
    public java.util.Date getUpdateDate() {
        return this.updateDate;
    }

    /**
     * 设置 最后一次修改时间
     */
    public void setUpdateDate(java.util.Date updateDate) {
        this.updateDate = updateDate;
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

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public int getRelease() {
        return release;
    }

    public void setRelease(int release) {
        this.release = release;
    }

    public String getClassifiedId() {
        return classifiedId;
    }

    public void setClassifiedId(String classifiedId) {
        this.classifiedId = classifiedId;
    }
}
