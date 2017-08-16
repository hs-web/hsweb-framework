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

package org.hswebframework.web.controller.organizational;

import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.GenericEntityController;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.entity.organizational.PersonAuthBindEntity;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.entity.organizational.PositionEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.service.organizational.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 人员
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.person:person}")
@Authorize(permission = "person")
@AccessLogger("人员")
public class PersonController implements SimpleGenericEntityController<PersonEntity, String, QueryParamEntity> {

    private PersonService personService;

    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public PersonService getService() {
        return personService;
    }

    @Override
    public ResponseMessage<PagerResult<PersonEntity>> list(QueryParamEntity param) {
        return SimpleGenericEntityController.super.list(param);
    }

    @GetMapping("/me")
    @AccessLogger("查看当前登录用户的人员信息")
    @Authorize(merge = false)
    public ResponseMessage<PersonAuthBindEntity> getLoginUserPerson() {
        PersonnelAuthorization authorization = PersonnelAuthorization
                .current()
                .orElseThrow(NotFoundException::new);
        return getDetail(authorization.getPersonnel().getId());
    }

    @GetMapping("/me/authorization")
    @AccessLogger("查看当前登录用户的人员权限信息")
    @Authorize(merge = false)
    public ResponseMessage<PersonnelAuthorization> getLoginUserPersonDetail() {
        PersonnelAuthorization authorization = PersonnelAuthorization
                .current()
                .orElseThrow(NotFoundException::new);
        return ResponseMessage.ok(authorization);
    }

    @GetMapping("/{id}/detail")
    @AccessLogger("查看人员详情")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<PersonAuthBindEntity> getDetail(@PathVariable String id) {
        return ResponseMessage.ok(personService.selectAuthBindByPk(id));
    }

    @PostMapping("/detail")
    @AccessLogger("新增人员信息,并关联用户信息")
    @Authorize(action = Permission.ACTION_ADD)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseMessage<String> getDetail(@RequestBody PersonAuthBindEntity bindEntity) {
        return ResponseMessage.ok(personService.insert(bindEntity));
    }

    @PutMapping("/{id}/detail")
    @AccessLogger("修改人员信息,并关联用户信息")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<String> getDetail(@PathVariable String id, @RequestBody PersonAuthBindEntity bindEntity) {
        bindEntity.setId(id);
        personService.updateByPk(bindEntity);
        return ResponseMessage.ok();
    }

    @GetMapping("/in-position/{positionId}")
    @AccessLogger("获取指定岗位的人员")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<List<PersonEntity>> getByPositionId(@PathVariable String positionId) {
        return ResponseMessage.ok(personService.selectByPositionId(positionId));
    }
}
