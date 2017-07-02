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

package org.hswebframework.web.example.oauth2;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.oauth2.server.entity.OAuth2ClientEntity;
import org.hswebframework.web.authorization.simple.SimpleFieldFilterDataAccessConfig;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.dao.oauth2.OAuth2ClientDao;
import org.hswebframework.web.entity.authorization.*;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.service.authorization.AuthorizationSettingService;
import org.hswebframework.web.service.authorization.PermissionService;
import org.hswebframework.web.service.authorization.RoleService;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;


/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@SpringBootApplication
@Configuration
@EnableCaching
public class OAuth2ServerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(OAuth2ServerApplication.class);
    }


    @Autowired
    UserService       userService;
    @Autowired
    RoleService       roleService;
    @Autowired
    PermissionService permissionService;
    @Autowired
    EntityFactory     entityFactory;
    @Autowired
    OAuth2ClientDao   oAuth2ClientDao;

    @Autowired
    AuthorizationSettingService authorizationSettingService;

    @Override
    public void run(String... strings) throws Exception {
        //添加示例数据，实际项目应该在前端进行维护
        /*
           ---------------------------------------------
           ------------------添加权限信息----------------
           -------------用户名: admin,密码: admin--------
           -------------角色: admin----------------------
           -------------权限: admin----------------------
         */
        //只能查询自己创建的数据
        DataAccessEntity accessEntity = new DataAccessEntity();
        accessEntity.setType(DataAccessConfig.DefaultType.OWN_CREATED);
        accessEntity.setAction(Permission.ACTION_QUERY);

        //只能修改自己创建的数据
        DataAccessEntity updateAccessEntity = new DataAccessEntity();
        updateAccessEntity.setType(DataAccessConfig.DefaultType.OWN_CREATED);
        updateAccessEntity.setAction(Permission.ACTION_UPDATE);

        DataAccessEntity denyFields = new DataAccessEntity();
        denyFields.setType(DataAccessConfig.DefaultType.DENY_FIELDS);
        denyFields.setAction(Permission.ACTION_UPDATE);
        denyFields.setConfig(JSON.toJSONString(new SimpleFieldFilterDataAccessConfig("password")));

        //脚本方式自定义控制
//        updateAccessEntity.setConfig(JSON.toJSONString(new SimpleScriptDataAccess("" +
//                "println(id);" +
//                "println(entity);" +
//                "println('脚本权限控制');" +
//                "return true;" +
//                "","groovy")));


        PermissionEntity permission = entityFactory.newInstance(PermissionEntity.class);
        permission.setName("测试");
        permission.setId("test");
        permission.setStatus((byte) 1);
        permission.setActions(ActionEntity.create(Permission.ACTION_QUERY, Permission.ACTION_UPDATE));
        permissionService.insert(permission);

        RoleEntity roleEntity = entityFactory.newInstance(RoleEntity.class);
        roleEntity.setId("admin");
        roleEntity.setName("test");
        roleService.insert(roleEntity);

          /*            权限设置        */
        AuthorizationSettingEntity settingEntity = entityFactory.newInstance(AuthorizationSettingEntity.class);

        settingEntity.setType("role"); //绑定到角色
        settingEntity.setSettingFor(roleEntity.getId());

        settingEntity.setDescribe("测试");
        //权限配置详情
        AuthorizationSettingDetailEntity detailEntity = entityFactory.newInstance(AuthorizationSettingDetailEntity.class);
        detailEntity.setPermissionId(permission.getId());
        detailEntity.setMerge(true);
        detailEntity.setPriority(1L);
        detailEntity.setActions(new HashSet<>(Arrays.asList(Permission.ACTION_QUERY, Permission.ACTION_UPDATE)));
        detailEntity.setDataAccesses(Arrays.asList(accessEntity, updateAccessEntity));

        settingEntity.setDetails(Arrays.asList(detailEntity));

        authorizationSettingService.insert(settingEntity);

        BindRoleUserEntity userEntity = entityFactory.newInstance(BindRoleUserEntity.class);
        userEntity.setId("admin");
        userEntity.setName("admin");
        userEntity.setCreateTimeNow();
        userEntity.setCreatorId("admin");
        userEntity.setUsername("admin");
        userEntity.setPassword("admin");
        userEntity.setRoles(Arrays.asList("admin"));
        userService.insert(userEntity);

        /*
            ------------------------添加OAuth2客户端---------------------
            ------------------client_id:hsweb_oauth2_example------------
            ------------------client_secret:hsweb_oauth2_example_secret--
         */
        OAuth2ClientEntity clientEntity = entityFactory.newInstance(OAuth2ClientEntity.class);

        clientEntity.setId("hsweb_oauth2_example");
        clientEntity.setSecret("hsweb_oauth2_example_secret");
        clientEntity.setOwnerId("admin");
        clientEntity.setName("测试");
        clientEntity.setType("test");
        clientEntity.setCreatorId("admin");
        // 这里与 hsweb-examples-oauth2-client 的回调地址对应
        clientEntity.setRedirectUri("http://localhost:8808/oauth2/callback/hsweb");
        clientEntity.setCreateTime(System.currentTimeMillis());
        clientEntity.setSupportGrantTypes(new HashSet<>(Collections.singletonList("*")));
        clientEntity.setStatus(DataStatus.STATUS_ENABLED);
        oAuth2ClientDao.insert(clientEntity);
    }

}
