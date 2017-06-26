/*
 *  Copyright 2016 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.hswebframework.web.entity.organizational;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.Set;

/**
 * 人员
 *
 * @author hsweb-generator-online
 */
public class SimplePersonEntity extends SimpleGenericEntity<String> implements PersonEntity {
    //姓名
    @NotBlank
    private String name;
    //性别
    private Byte   sex;
    //电子邮箱
    @Email
    private String email;
    //联系电话
    private String phone;
    //照片
    private String photo;
    //关联用户id
    private String userId;
    //状态
    private Byte   status;
    //备注
    private String remark;

    /**
     * @return 姓名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 性别
     */
    public Byte getSex() {
        return this.sex;
    }

    /**
     * 设置 性别
     */
    public void setSex(Byte sex) {
        this.sex = sex;
    }

    /**
     * @return 电子邮箱
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * 设置 电子邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return 联系电话
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * 设置 联系电话
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return 照片
     */
    public String getPhoto() {
        return this.photo;
    }

    /**
     * 设置 照片
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     * @return 关联用户id
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * 设置 关联用户id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return 状态
     */
    public Byte getStatus() {
        return this.status;
    }

    /**
     * 设置 状态
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    /**
     * @return 备注
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * 设置 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

}