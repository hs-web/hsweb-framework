/*
 *  Copyright 2019 http://www.hswebframework.org
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
package org.hswebframework.web.service.organizational;

import org.hswebframework.web.entity.organizational.PersonAuthBindEntity;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 * 人员 服务类
 *
 * @author hsweb-generator-online
 */
public interface PersonService extends CrudService<PersonEntity, String> {

    String insert(PersonAuthBindEntity authBindEntity);

    int updateByPk(PersonAuthBindEntity authBindEntity);

    List<PersonEntity> selectByName(String name);

    PersonAuthBindEntity selectAuthBindByPk(String id);

    List<PersonEntity> selectByPositionId(String positionId);

    List<PersonEntity> selectByPositionIds(List<String> positionId);

    List<PersonEntity> selectByDepartmentId(List<String> departmentId);

    List<PersonEntity> selectByOrgId(List<String> departmentId);

    PersonEntity selectByUserId(String userId);

    List<String> selectAllDepartmentId(List<String> personId);

    List<String> selectAllOrgId(List<String> personId);

    List<PersonEntity> selectByRoleId(String roleId);
}
