package org.hswebframework.web.example.custom.entity;

import org.hswebframework.web.entity.organizational.SimpleOrganizationalEntity;

/**
 * 自定义实体
 *
 * @author zhouhao
 */
public class CustomOrganizationalEntity extends SimpleOrganizationalEntity {
    private String leader;

    private String nameEn;

    private String otherProperty;

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getOtherProperty() {
        return otherProperty;
    }

    public void setOtherProperty(String otherProperty) {
        this.otherProperty = otherProperty;
    }
}
