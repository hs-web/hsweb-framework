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

package org.hswebframework.web.controller.authorization;

import io.swagger.annotations.*;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.*;
import org.hswebframework.web.entity.authorization.bind.BindPermissionRoleEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.model.authorization.*;
import org.hswebframework.web.service.authorization.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.hswebframework.web.controller.QueryController.*;
import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * 角色控制器
 *
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.role:role}")
@AccessLogger("{role_manager}")
@Authorize(permission = "role")
@Api(tags = "role-manager", description = "角色管理")
public class RoleController implements QueryController<RoleEntity, String, QueryParamEntity> {

    @Autowired
    private RoleService roleService;

    @Autowired
    private EntityFactory entityFactory;

    @Override
    public RoleService getService() {
        return roleService;
    }

    @SuppressWarnings("unchecked")
    public BindPermissionRoleEntity<PermissionRoleEntity> modelToEntity(RoleModel roleModel) {
        BindPermissionRoleEntity<PermissionRoleEntity> roleEntity = entityFactory.newInstance(BindPermissionRoleEntity.class);
        roleEntity.setId(roleModel.getId());
        roleEntity.setName(roleModel.getName());
        roleEntity.setDescribe(roleModel.getDescribe());
        List<PermissionRoleEntity> permissionRoleEntities =
                roleModel.getPermissions().stream()
                        .map(model -> {
                            PermissionRoleEntity entity = entityFactory.newInstance(PermissionRoleEntity.class);
                            entity.setActions(model.getActions());
                            entity.setPermissionId(model.getPermissionId());
                            entity.setRoleId(roleModel.getId());
                            //copy field accesses
                            entity.setFieldAccesses(model.getFieldAccesses().stream().map(accessModel -> {
                                FieldAccessEntity accessEntity = new FieldAccessEntity();
                                accessEntity.setField(accessEntity.getField());
                                accessEntity.setDescribe(accessEntity.getDescribe());
                                accessEntity.setActions(accessModel.getActions().stream().map(actionModel -> {
                                    ActionEntity actionEntity = new ActionEntity();
                                    actionEntity.setAction(actionModel.getAction());
                                    actionEntity.setDescribe(actionModel.getDescribe());
                                    actionEntity.setDefaultCheck(actionModel.isDefaultCheck());
                                    return actionEntity;
                                }).collect(Collectors.toList()));
                                return accessEntity;
                            }).collect(Collectors.toList()));
                            //copy data accesses
                            entity.setDataAccesses(model.getDataAccesses().stream().map(accessModel -> {
                                DataAccessEntity dataAccessEntity = new DataAccessEntity();
                                dataAccessEntity.setConfig(accessModel.getConfig());
                                dataAccessEntity.setType(accessModel.getType());
                                dataAccessEntity.setDescribe(accessModel.getDescribe());
                                dataAccessEntity.setAction(accessModel.getAction());
                                return dataAccessEntity;
                            }).collect(Collectors.toList()));
                            return entity;
                        }).collect(Collectors.toList());
        roleEntity.setPermissions(permissionRoleEntities);
        return roleEntity;
    }

    protected RoleModel entityToModel(RoleEntity roleEntity) {
        RoleModel roleModel = entityFactory.newInstance(RoleModel.class);
        roleModel.setId(roleEntity.getId());
        roleModel.setDescribe(roleEntity.getDescribe());
        roleModel.setName(roleEntity.getName());
        if (roleEntity instanceof BindPermissionRoleEntity) {
            BindPermissionRoleEntity<PermissionRoleEntity> permissionRoleEntity = (BindPermissionRoleEntity) roleEntity;
            List<PermissionRoleEntity> roleEntities = permissionRoleEntity.getPermissions();
            if (CollectionUtils.isNotEmpty(roleEntities)) {
                List<PermissionRoleModel> roleModels = roleEntities.stream().map(entity -> {
                    PermissionRoleModel model = entityFactory.newInstance(PermissionRoleModel.class);
                    model.setActions(entity.getActions());
                    model.setPermissionId(entity.getPermissionId());
                    //copy field accesses
                    model.setFieldAccesses(entity.getFieldAccesses().stream().map(accessEntity -> {
                        FieldAccessModel accessModel = new FieldAccessModel();
                        accessModel.setField(accessModel.getField());
                        accessModel.setDescribe(accessModel.getDescribe());
                        accessModel.setActions(accessModel.getActions().stream().map(actionEntity -> {
                            ActionModel actionModel = new ActionModel();
                            actionModel.setAction(actionEntity.getAction());
                            actionModel.setDescribe(actionEntity.getDescribe());
                            actionModel.setDefaultCheck(actionEntity.isDefaultCheck());
                            return actionModel;
                        }).collect(Collectors.toList()));
                        return accessModel;
                    }).collect(Collectors.toList()));
                    //copy data accesses
                    model.setDataAccesses(model.getDataAccesses().stream().map(accessesEntity -> {
                        DataAccessModel dataAccessModel = new DataAccessModel();
                        dataAccessModel.setConfig(accessesEntity.getConfig());
                        dataAccessModel.setType(accessesEntity.getType());
                        dataAccessModel.setDescribe(accessesEntity.getDescribe());
                        dataAccessModel.setAction(accessesEntity.getAction());
                        return dataAccessModel;
                    }).collect(Collectors.toList()));
                    return model;
                }).collect(Collectors.toList());
                roleModel.setPermissions(roleModels);
            }
        }

        return roleModel;
    }

    @Authorize(action = Permission.ACTION_GET)
    @GetMapping(path = "/{id:.+}/detail")
    @AccessLogger("{get_by_id}")
    @ApiOperation("根据主键查询完整数据")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功"),
            @ApiResponse(code = 401, message = "未授权"),
            @ApiResponse(code = 403, message = "无权限"),
            @ApiResponse(code = 404, message = "数据不存在")
    })
    public ResponseMessage<RoleModel> getDetailByPrimaryKey(@PathVariable String id) {
        return ok(entityToModel(assertNotNull(getService().selectByPk(id))));
    }

    @PostMapping
    @Authorize(action = Permission.ACTION_ADD)
    @AccessLogger("{add}")
    @ApiOperation("添加角色")
    public ResponseMessage<String> addRole(@RequestBody RoleModel roleModel) {
        return ok(roleService.insert(modelToEntity(roleModel)));
    }

    @PutMapping("/{id:.+}")
    @Authorize(action = Permission.ACTION_UPDATE)
    @AccessLogger("{update}")
    @ApiModelProperty("修改角色")
    public ResponseMessage updateRole(@PathVariable String id, @RequestBody RoleModel roleModel) {
        roleModel.setId(id);
        roleService.updateByPrimaryKey(modelToEntity(roleModel));
        return ok();
    }

    @PutMapping("/disable/{id:.+}")
    @Authorize(action = Permission.ACTION_DISABLE)
    @AccessLogger("{disable}")
    @ApiModelProperty("禁用角色")
    public ResponseMessage disable(@PathVariable String id) {
        roleService.disable(id);
        return ok();
    }

    @PutMapping("/enable/{id}")
    @Authorize(action = Permission.ACTION_ENABLE)
    @AccessLogger("{disable}")
    @ApiModelProperty("启用角色")
    public ResponseMessage enable(@PathVariable String id) {
        roleService.enable(id);
        return ok();
    }
}
