package org.hsweb.web.bean.po.draft;

import org.hsweb.web.bean.po.GenericPo;

import java.util.Date;

/**
 * Created by zhouhao on 16-6-2.
 */
public class Draft extends GenericPo<String> {
    private String name;

    private Object value;

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
}
