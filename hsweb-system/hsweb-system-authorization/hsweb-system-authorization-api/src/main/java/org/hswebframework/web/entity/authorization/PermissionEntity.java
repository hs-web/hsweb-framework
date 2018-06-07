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
 *
 */

package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.GenericEntity;

import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @author zhouhao
 */
public interface PermissionEntity extends GenericEntity<String> {

    @Override
    @Pattern(regexp = "[a-zA-Z0-9_\\-]+")
    String getId();

    String getName();

    String getDescribe();

    Byte getStatus();

    void setStatus(Byte status);

    String getType();

    void setType(String type);

    void setName(String name);

    void setDescribe(String comment);

    List<ActionEntity> getActions();

    void setActions(List<ActionEntity> actions);

    /**
     * 此权限支持的数据权限类型,此字段只用于前端使用,在分配权限的时候,可以通过此字段来展示相应的数据权限设置,后台并没有使用此字段
     * @return 支持的数据权限类型
     */
    List<String> getSupportDataAccessTypes();

    void setSupportDataAccessTypes(List<String> supportDataAccessTypes);

    void setOptionalFields(List<OptionalField> fields);

    List<OptionalField> getOptionalFields();

    //直接关联其他权限
    List<ParentPermission> getParents();

}
