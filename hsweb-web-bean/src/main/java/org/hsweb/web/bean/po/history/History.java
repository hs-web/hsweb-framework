package org.hsweb.web.bean.po.history;

import org.hsweb.commons.MD5;
import org.hsweb.web.bean.po.GenericPo;

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
    private String primaryKeyName;

    /**
     * 操作数据的主键值
     */
    private String primaryKeyValue;

    /**
     * 操作前记录
     */
    private String changeBefore;

    /**
     * 操作后记录
     */
    private String changeAfter;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 创建人主键
     */
    private String creatorId;

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

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public String getPrimaryKeyValue() {
        return primaryKeyValue;
    }

    public void setPrimaryKeyValue(String primaryKeyValue) {
        this.primaryKeyValue = primaryKeyValue;
    }

    public String getChangeBefore() {
        return changeBefore;
    }

    public void setChangeBefore(String changeBefore) {
        this.changeBefore = changeBefore;
    }

    public String getChangeAfter() {
        return changeAfter;
    }

    public void setChangeAfter(String changeAfter) {
        this.changeAfter = changeAfter;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public static History newInstance(String type) {
        History history = new History(type);
        history.setId(MD5.encode(UUID.randomUUID().toString().concat(String.valueOf(Math.random()))));
        history.setCreateDate(new Date());
        history.setCreatorId("Sys");
        return history;
    }



public interface Property extends GenericPo.Property{
	/**
	 *
	 * @see History#type
	 */
	String type="type";
	/**
	 *
	 * @see History#describe
	 */
	String describe="describe";
	/**
	 *
	 * @see History#primaryKeyName
	 */
	String primaryKeyName="primaryKeyName";
	/**
	 *
	 * @see History#primaryKeyValue
	 */
	String primaryKeyValue="primaryKeyValue";
	/**
	 *
	 * @see History#changeBefore
	 */
	String changeBefore="changeBefore";
	/**
	 *
	 * @see History#changeAfter
	 */
	String changeAfter="changeAfter";
	/**
	 *
	 * @see History#createDate
	 */
	String createDate="createDate";
	/**
	 *
	 * @see History#creatorId
	 */
	String creatorId="creatorId";
	}
}