package org.hswebframework.web.organizational.authorization.simple;

import org.hswebframework.web.organizational.authorization.Personnel;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePersonnel implements Personnel {
    private String id;
    private String name;
    private String phone;
    private String photo;
    private String email;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
