package org.hsweb.web.bean.po.draft;

import org.hsweb.web.bean.po.GenericPo;

import java.util.Date;

public class Draft extends GenericPo<String> {
    private String name;

    private Object value;

    private String key;

    private Date createDate;

    private String creatorId;

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }



public interface Property extends GenericPo.Property{
	/**
	 *
	 * @see Draft#name
	 */
	String name="name";
	/**
	 *
	 * @see Draft#value
	 */
	String value="value";
	/**
	 *
	 * @see Draft#key
	 */
	String key="key";
	/**
	 *
	 * @see Draft#createDate
	 */
	String createDate="createDate";
	/**
	 *
	 * @see Draft#creatorId
	 */
	String creatorId="creatorId";
	}
}