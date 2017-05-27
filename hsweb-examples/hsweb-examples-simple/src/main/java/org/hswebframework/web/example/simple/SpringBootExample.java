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
import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.oauth2.server.entity.OAuth2ClientEntity;
import org.hswebframework.web.authorization.simple.SimpleFieldFilterDataAccessConfig;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.dao.datasource.DataSourceHolder;
import org.hswebframework.web.dao.datasource.DatabaseType;
import org.hswebframework.web.dao.oauth2.OAuth2ClientDao;
import org.hswebframework.web.entity.authorization.*;
import org.hswebframework.web.entity.authorization.bind.BindPermissionRoleEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.loggin.aop.EnableAccessLogger;
import org.hswebframework.web.logging.AccessLoggerListener;
import org.hswebframework.web.service.authorization.PermissionService;
import org.hswebframework.web.service.authorization.RoleService;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.datasource.DataSourceUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

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
public class SpringBootExample implements CommandLineRunner {

    @Bean
    public AccessLoggerListener accessLoggerListener() {
        return loggerInfo -> System.out.println("有请求啦:" + JSON.toJSONString(loggerInfo.getAction()));
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


    @Bean
    @ConditionalOnMissingBean(SqlExecutor.class)
    public SqlExecutor sqlExecutor(DataSource dataSource) {
        DataSourceHolder.install(dataSource, DatabaseType.h2);
        return new AbstractJdbcSqlExecutor() {
            @Override
            public Connection getConnection() {
                return DataSourceUtils.getConnection(dataSource);
            }

            @Override
            public void releaseConnection(Connection connection) throws SQLException {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        };

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

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExample.class);
    }

    @Override
    public void run(String... strings) throws Exception {
        //只能查询自己创建的数据
        DataAccessEntity accessEntity = new DataAccessEntity();
        accessEntity.setType(DataAccessConfig.DefaultType.OWN_CREATED);
        accessEntity.setAction(Permission.ACTION_QUERY);

        //只能修改自己创建的数据
        DataAccessEntity updateAccessEntity = new DataAccessEntity();
        updateAccessEntity.setType(DataAccessConfig.DefaultType.OWN_CREATED);
        updateAccessEntity.setAction(Permission.ACTION_UPDATE);

        //不能查询password
        DataAccessEntity denyQueryFields = new DataAccessEntity();
        denyQueryFields.setType(DataAccessConfig.DefaultType.ALLOW_FIELDS);
        denyQueryFields.setAction(Permission.ACTION_QUERY);
        denyQueryFields.setConfig(JSON.toJSONString(new SimpleFieldFilterDataAccessConfig("password")));

        //不能修改password
        DataAccessEntity denyUpdateFields = new DataAccessEntity();
        denyUpdateFields.setType(DataAccessConfig.DefaultType.ALLOW_FIELDS);
        denyUpdateFields.setAction(Permission.ACTION_UPDATE);
        denyUpdateFields.setConfig(JSON.toJSONString(new SimpleFieldFilterDataAccessConfig("password")));


        PermissionEntity permission = entityFactory.newInstance(PermissionEntity.class);
        permission.setName("测试");
        permission.setId("test");
        permission.setStatus((byte) 1);
        permission.setActions(ActionEntity.create(Permission.ACTION_QUERY, Permission.ACTION_UPDATE));
        permission.setDataAccess(Arrays.asList(accessEntity, updateAccessEntity, denyUpdateFields,denyUpdateFields));
        permissionService.insert(permission);

        BindPermissionRoleEntity<PermissionRoleEntity> roleEntity = entityFactory.newInstance(BindPermissionRoleEntity.class);
        SimplePermissionRoleEntity permissionRoleEntity = new SimplePermissionRoleEntity();
        permissionRoleEntity.setRoleId("admin");
        permissionRoleEntity.setPermissionId("test");
        permissionRoleEntity.setActions(Arrays.asList(Permission.ACTION_QUERY, Permission.ACTION_UPDATE));
        permissionRoleEntity.setDataAccesses(permission.getDataAccess());
        roleEntity.setId("admin");
        roleEntity.setName("test");
        roleEntity.setPermissions(Arrays.asList(permissionRoleEntity));
        roleService.insert(roleEntity);

        BindRoleUserEntity userEntity = entityFactory.newInstance(BindRoleUserEntity.class);
        userEntity.setId("admin");
        userEntity.setName("admin");
        userEntity.setCreateTimeNow();
        userEntity.setCreatorId("admin");
        userEntity.setUsername("admin");
        userEntity.setPassword("admin");
        userEntity.setRoles(Arrays.asList("admin"));
        userService.insert(userEntity);

        OAuth2ClientEntity clientEntity = entityFactory.newInstance(OAuth2ClientEntity.class);

        clientEntity.setId("test");
        clientEntity.setSecret("test");
        clientEntity.setOwnerId("admin");
        clientEntity.setName("测试");
        clientEntity.setType("test");
        clientEntity.setCreatorId("admin");
        clientEntity.setRedirectUri("http://localhost");
        clientEntity.setCreateTime(System.currentTimeMillis());
        clientEntity.setSupportGrantTypes(Collections.singleton("*"));
        oAuth2ClientDao.insert(clientEntity);
    }
}
