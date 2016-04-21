package org.hsweb.web.bean.po.config;

import org.hsweb.web.bean.po.GenericPo;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * 系统配置
 * Created by generator
 */
public class Config extends GenericPo<String> {

    private static final long serialVersionUID = 5328848488856425388L;

    //备注
    private String remark;

    //配置内容
    private String content;

    //创建日期
    private java.util.Date create_date;

    //最后一次修改日期
    private java.util.Date update_date;

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
     * 获取 配置内容
     *
     * @return String 配置内容
     */
    public String getContent() {
        if (this.content == null)
            return "";
        return this.content;
    }

    /**
     * 设置 配置内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取 创建日期
     *
     * @return java.util.Date 创建日期
     */
    public java.util.Date getCreate_date() {
        return this.create_date;
    }

    /**
     * 设置 创建日期
     */
    public void setCreate_date(java.util.Date create_date) {
        this.create_date = create_date;
    }

    /**
     * 获取 最后一次修改日期
     *
     * @return java.util.Date 最后一次修改日期
     */
    public java.util.Date getUpdate_date() {
        return this.update_date;
    }

    /**
     * 设置 最后一次修改日期
     */
    public void setUpdate_date(java.util.Date update_date) {
        this.update_date = update_date;
    }

    public Properties toMap() {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
