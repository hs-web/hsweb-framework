/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.entity.authorization;

import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.validator.group.CreateGroup;

/**
 * @author zhouhao
 */
public interface UserEntity extends GenericEntity<String>, RecordCreationEntity {
    String name = "name";
    String username = "username";
    String salt = "salt";
    @SuppressWarnings("all")
    String password = "password";
    String status = "status";

    void setName(String name);

    /**
     * @return 用户名, 只读, 只能新增, 不能修改
     */
    String getUsername();

    void setUsername(String username);

    @NotBlank(groups = CreateGroup.class)
    String getName();

    void setPassword(String password);

    String getPassword();

    void setSalt(String salt);

    String getSalt();

    /**
     * @return 数据状态
     * @see org.hswebframework.web.commons.entity.DataStatus
     */
    Byte getStatus();

    void setStatus(Byte status);

    @Override
    UserEntity clone();
}
