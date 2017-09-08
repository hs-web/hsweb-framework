/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.example.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionCustomizerParser;
import org.hswebframework.web.authorization.basic.configuration.EnableAopAuthorize;
import org.hswebframework.web.authorization.basic.define.EmptyAuthorizeDefinition;
import org.hswebframework.web.authorization.basic.web.UserTokenHolder;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.simple.SimpleFieldFilterDataAccessConfig;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.entity.authorization.*;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.entity.organizational.*;
import org.hswebframework.web.loggin.aop.EnableAccessLogger;
import org.hswebframework.web.logging.AccessLoggerListener;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.simple.SimpleScopeDataAccessConfig;
import org.hswebframework.web.service.authorization.AuthorizationSettingService;
import org.hswebframework.web.service.authorization.PermissionService;
import org.hswebframework.web.service.authorization.RoleService;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.organizational.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@SpringBootApplication
@Configuration
@EnableSwagger2
@EnableCaching
@EnableAspectJAutoProxy
@EnableAccessLogger
@EnableAopAuthorize
public class SpringBootExample
        implements CommandLineRunner {

    @Bean
    public AopMethodAuthorizeDefinitionCustomizerParser customizerParser(){
        //自定义权限声明
        return context -> EmptyAuthorizeDefinition.instance;
    }

    @Bean
    public AccessLoggerListener accessLoggerListener() {
        Class excludes[] = {
                ServletRequest.class,
                ServletResponse.class,
                InputStream.class,
                OutputStream.class,
                MultipartFile.class,
                MultipartFile[].class
        };
        return loggerInfo -> {
            Map<String, Object> loggerMap = loggerInfo.toSimpleMap(obj -> {
                if (Stream.of(excludes).anyMatch(aClass -> aClass.isInstance(obj)))
                    return obj.getClass().getName();
                return JSON.toJSONString(obj);
            });
//            loggerMap.put("userToken", UserTokenHolder.currentToken());

            System.out.println(JSON.toJSONString(loggerMap, SerializerFeature.SortField, SerializerFeature.PrettyFormat));

        };
    }

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("example")
                .ignoredParameterTypes(HttpSession.class, Authentication.class, HttpServletRequest.class, HttpServletResponse.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.hswebframework.web"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("hsweb 3.0 api")
                .description("hsweb 企业后台管理基础框架")
                .termsOfServiceUrl("http://www.hsweb.me/")
                .license("apache 2.0")
                .version("3.0")
                .build();
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
    OrganizationalService organizationalService;
    @Autowired
    DepartmentService     departmentService;
    @Autowired
    PositionService       positionService;
    @Autowired
    PersonService         personService;

    @Autowired
    AuthorizationSettingService authorizationSettingService;

    @Autowired
    RelationInfoService relationInfoService;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExample.class);
    }

    // main
    //    @Override
    public void run(String... strings) throws Exception {
        //只能查询自己创建的数据
        DataAccessEntity accessEntity = new DataAccessEntity();
        accessEntity.setType(DataAccessConfig.DefaultType.OWN_CREATED);
        accessEntity.setAction(Permission.ACTION_QUERY);
        accessEntity.setDescribe("只能查询自己创建的数据");

        //只能修改自己创建的数据
        DataAccessEntity updateAccessEntity = new DataAccessEntity();
        updateAccessEntity.setType(DataAccessConfig.DefaultType.OWN_CREATED);
        updateAccessEntity.setAction(Permission.ACTION_UPDATE);
        updateAccessEntity.setDescribe("只能修改自己的数据");
        //不能查询password
        DataAccessEntity denyQueryFields = new DataAccessEntity();
        denyQueryFields.setType(DataAccessConfig.DefaultType.DENY_FIELDS);
        denyQueryFields.setAction(Permission.ACTION_QUERY);
        denyQueryFields.setConfig(JSON.toJSONString(new SimpleFieldFilterDataAccessConfig("password")));
        denyQueryFields.setDescribe("不能查询密码");
        //不能修改password
        DataAccessEntity denyUpdateFields = new DataAccessEntity();
        denyUpdateFields.setType(DataAccessConfig.DefaultType.DENY_FIELDS);
        denyUpdateFields.setAction(Permission.ACTION_UPDATE);
        denyUpdateFields.setConfig(JSON.toJSONString(new SimpleFieldFilterDataAccessConfig("password")));
        denyUpdateFields.setDescribe("不能直接修改密码");
        //只能查看自己部门的数据
        DataAccessEntity onlyDepartmentData = new DataAccessEntity();
        onlyDepartmentData.setType(DataAccessType.DEPARTMENT_SCOPE);
        onlyDepartmentData.setAction(Permission.ACTION_QUERY);
        onlyDepartmentData.setConfig(JSON.toJSONString(new SimpleScopeDataAccessConfig(DataAccessType.SCOPE_TYPE_CHILDREN)));
        onlyDepartmentData.setDescribe("只能查看自己部门的数据");

        //创建权限
        PermissionEntity permission = entityFactory.newInstance(PermissionEntity.class);
        permission.setName("测试");
        permission.setId("test");
        permission.setStatus(DataStatus.STATUS_ENABLED);
        permission.setActions(ActionEntity.create(Permission.ACTION_QUERY, Permission.ACTION_UPDATE));
        permission.setSupportDataAccessTypes(Arrays.asList("*"));
        permissionService.insert(permission);

        //角色
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
        detailEntity.setDataAccesses(Arrays.asList(accessEntity, updateAccessEntity, denyQueryFields, denyUpdateFields, onlyDepartmentData));

        settingEntity.setDetails(Arrays.asList(detailEntity));

        authorizationSettingService.insert(settingEntity);

        //关联角色给用户
        BindRoleUserEntity userEntity = entityFactory.newInstance(BindRoleUserEntity.class);
        userEntity.setId("admin");
        userEntity.setName("admin");
        userEntity.setCreateTimeNow();
        userEntity.setCreatorId("admin");
        userEntity.setUsername("admin");
        userEntity.setPassword("admin");
        userEntity.setRoles(Arrays.asList("admin"));
        userService.insert(userEntity);


        OrganizationalEntity org = entityFactory.newInstance(OrganizationalEntity.class);

        org.setName("测试机构");
        org.setStatus(DataStatus.STATUS_ENABLED);
        org.setId("test");
        org.setParentId("-1");

        organizationalService.insert(org);

        DepartmentEntity department = entityFactory.newInstance(DepartmentEntity.class);
        department.setStatus(DataStatus.STATUS_ENABLED);
        department.setOrgId("test");
        department.setId("test");
        department.setName("部门");
        department.setParentId("-1");

        DepartmentEntity department2 = entityFactory.newInstance(DepartmentEntity.class);
        department2.setStatus(DataStatus.STATUS_ENABLED);
        department2.setOrgId("test");
        department2.setId("test2");
        department2.setName("部门2");
        department2.setParentId("test");
        department.setChildren(Collections.singletonList(department2));
        departmentService.insert(department);


        PositionEntity position = entityFactory.newInstance(PositionEntity.class);
        position.setName("职务");
        position.setId("test");
        position.setDepartmentId("test");
        position.setParentId("-1");
        position.setRoles(Collections.singletonList("admin"));
        positionService.insert(position);

        PersonAuthBindEntity personEntity = entityFactory.newInstance(PersonAuthBindEntity.class);
        personEntity.setName("测试人员");
        personEntity.setPositionIds(Collections.singleton(position.getId()));
        personEntity.setUserId(userEntity.getId());

        PersonUserEntity personUserEntity = new PersonUserEntity();
        personUserEntity.setUsername("admin");
        personEntity.setPersonUser(personUserEntity);

        personService.insert(personEntity);

        RelationInfoEntity relationInfo = relationInfoService.createEntity();

        relationInfo.setRelationFrom(personEntity.getId());
        relationInfo.setRelationTo("zhangsan");
        relationInfo.setRelationTypeFrom("person");
        relationInfo.setRelationTypeTo("person");
        relationInfo.setStatus(DataStatus.STATUS_ENABLED);
        relationInfo.setRelationId("leader");
        relationInfoService.insert(relationInfo);

//        relationInfoService
//                .getRelations("person","王伟")
//                .findRev("直属上级");
    }
}
