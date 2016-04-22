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

    //是否正在使用
    private boolean using;

    //创建时间
    private java.util.Date create_date;

    //最后一次修改时间
    private java.util.Date update_date;


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

    public static void main(String[] args) {
        System.out.println("AA2_2".matches("(^[a-z][a-z0-9_]*$)|(^[A-Z][A-Z0-9_]*$)"));
    }
}
