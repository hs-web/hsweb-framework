package org.hsweb.web.bean.po.history;

import org.hsweb.web.bean.po.GenericPo;
import org.webbuilder.utils.common.MD5;

import java.util.Date;
import java.util.UUID;

/**
 * 操作历史记录
 * Created by zhouhao on 16-4-22.
 */
public class History extends GenericPo<String> {

    /**
     * 记录类型
     */
    private String type;

    /**
     * 说明
     */
    private String describe;

    /**
     * 操作数据的主键名称
     */
    private String primary_key_name;

    /**
     * 操作数据的主键值
     */
    private String primary_key_value;

    /**
     * 操作前记录
     */
    private String change_before;

    /**
     * 操作后记录
     */
    private String change_after;

    /**
     * 创建日期
     */
    private Date create_date;

    /**
     * 创建人主键
     */
    private String creator_id;

    public History() {
    }

    public History(String type) {
        this.type = type;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getPrimary_key_name() {
        return primary_key_name;
    }

    public void setPrimary_key_name(String primary_key_name) {
        this.primary_key_name = primary_key_name;
    }

    public String getPrimary_key_value() {
        return primary_key_value;
    }

    public void setPrimary_key_value(String primary_key_value) {
        this.primary_key_value = primary_key_value;
    }

    public String getChange_before() {
        return change_before;
    }

    public void setChange_before(String change_before) {
        this.change_before = change_before;
    }

    public String getChange_after() {
        return change_after;
    }

    public void setChange_after(String change_after) {
        this.change_after = change_after;
    }

    public Date getCreate_date() {
        return create_date;
    }

    public void setCreate_date(Date create_date) {
        this.create_date = create_date;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public static History newInstace(String type) {
        History history = new History(type);
        history.setU_id(MD5.encode(UUID.randomUUID().toString().concat(String.valueOf(Math.random()))));
        history.setCreate_date(new Date());
        history.setCreator_id("_sys");
        return history;
    }
}
