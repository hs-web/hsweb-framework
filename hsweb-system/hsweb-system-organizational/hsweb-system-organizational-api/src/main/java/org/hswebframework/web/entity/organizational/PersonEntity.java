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

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * 人员 实体
 *
 * @author hsweb-generator-online
 */
public interface PersonEntity extends GenericEntity<String> {
  /*------------------------------------------
    |               属性名常量               |
    =========================================*/
    /**
     * 姓名
     */
    String name   = "name";
    /**
     * 性别
     */
    String sex    = "sex";
    /**
     * 电子邮箱
     */
    String email  = "email";
    /**
     * 联系电话
     */
    String phone  = "phone";
    /**
     * 照片
     */
    String photo  = "photo";
    /**
     * 关联用户id
     */
    String userId = "userId";
    /**
     * 状态
     */
    String status = "status";
    /**
     * 备注
     */
    String remark = "remark";

    /**
     * @return 姓名
     */
    String getName();

    /**
     * 设置 姓名
     */
    void setName(String name);

    /**
     * @return 性别
     */
    Byte getSex();

    /**
     * 设置 性别
     */
    void setSex(Byte sex);

    /**
     * @return 电子邮箱
     */
    String getEmail();

    /**
     * 设置 电子邮箱
     */
    void setEmail(String email);

    /**
     * @return 联系电话
     */
    String getPhone();

    /**
     * 设置 联系电话
     */
    void setPhone(String phone);

    /**
     * @return 照片
     */
    String getPhoto();

    /**
     * 设置 照片
     */
    void setPhoto(String photo);

    /**
     * @return 关联用户id
     */
    String getUserId();

    /**
     * 设置 关联用户id
     */
    void setUserId(String userId);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * 设置 状态
     */
    void setStatus(Byte status);

    /**
     * @return 备注
     */
    String getRemark();

    /**
     * 设置 备注
     */
    void setRemark(String remark);

}