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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.organizational.PersonAuthBindEntity;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
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
@Authorize(permission = "person",description = "人员管理")
@Api(value = "人员管理",tags = "组织架构-人员管理")
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
    @ApiOperation("查看当前登录用户的人员信息")
    @Authorize(merge = false)
    public ResponseMessage<PersonAuthBindEntity> getLoginUserPerson() {
        PersonnelAuthentication authorization = PersonnelAuthentication
                .current()
                .orElseThrow(NotFoundException::new);
        return getDetail(authorization.getPersonnel().getId());
    }

    @PutMapping("/me")
    @ApiOperation("修改个人信息")
    @Authorize(merge = false)
    public ResponseMessage<String> updateMePersonInfo(@RequestBody PersonAuthBindEntity bindEntity) {
        PersonnelAuthentication authorization = PersonnelAuthentication
                .current()
                .orElseThrow(NotFoundException::new);
        PersonAuthBindEntity old = personService
                .selectAuthBindByPk(authorization.getPersonnel().getId());

        bindEntity.setUserId(old.getUserId());
        bindEntity.setId(old.getId());
        bindEntity.setPositionIds(null);

        if (bindEntity.getPersonUser() != null) {
            bindEntity.getPersonUser().setUsername(old.getPersonUser().getUsername());
        }

        personService.updateByPk(bindEntity);
        return ResponseMessage.ok();
    }

    @GetMapping("/me/authorization")
    @ApiOperation("查看当前登录用户的人员权限信息")
    @Authorize(merge = false)
    public ResponseMessage<PersonnelAuthentication> getLoginUserPersonDetail() {
        PersonnelAuthentication authorization = PersonnelAuthentication
                .current()
                .orElseThrow(NotFoundException::new);
        return ResponseMessage.ok(authorization);
    }

    @GetMapping("/{id}/detail")
    @ApiOperation("查看人员详情")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<PersonAuthBindEntity> getDetail(@PathVariable String id) {
        return ResponseMessage.ok(personService.selectAuthBindByPk(id));
    }

    @PostMapping("/detail")
    @ApiOperation("新增人员信息,并关联用户信息")
    @Authorize(action = Permission.ACTION_ADD)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseMessage<String> getDetail(@RequestBody PersonAuthBindEntity bindEntity) {
        return ResponseMessage.ok(personService.insert(bindEntity));
    }

    @PutMapping("/{id}/detail")
    @ApiOperation("修改人员信息,并关联用户信息")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<String> getDetail(@PathVariable String id, @RequestBody PersonAuthBindEntity bindEntity) {
        bindEntity.setId(id);
        personService.updateByPk(bindEntity);
        return ResponseMessage.ok();
    }

    @GetMapping("/in-position/{positionId}")
    @ApiOperation("获取指定岗位的人员")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<List<PersonEntity>> getByPositionId(@PathVariable String positionId) {
        return ResponseMessage.ok(personService.selectByPositionId(positionId));
    }
}
