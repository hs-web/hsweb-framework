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

import org.hsweb.ezorm.rdb.executor.AbstractJdbcSqlExecutor;
import org.hsweb.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccess;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.dao.authorization.oauth2.OAuth2ClientDao;
import org.hswebframework.web.dao.datasource.DataSourceHolder;
import org.hswebframework.web.dao.datasource.DatabaseType;
import org.hswebframework.web.entity.authorization.*;
import org.hswebframework.web.entity.authorization.bind.BindPermissionRoleEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.entity.authorization.oauth2.OAuth2ClientEntity;
import org.hswebframework.web.service.authorization.PermissionService;
import org.hswebframework.web.service.authorization.RoleService;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@SpringBootApplication
@Configuration
@EnableSwagger2
public class SpringBootExample implements CommandLineRunner {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("example")
                .ignoredParameterTypes(HttpSession.class, Authorization.class, HttpServletRequest.class, HttpServletResponse.class)
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
    OAuth2ClientDao oAuth2ClientDao;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExample.class);
    }

    @Override
    public void run(String... strings) throws Exception {
        //只能查询自己创建的数据
        DataAccessEntity accessEntity = new DataAccessEntity();
        accessEntity.setType(DataAccess.Type.OWN_CREATED.name());
        accessEntity.setAction(Permission.ACTION_QUERY);

        //只能修改自己创建的数据
        DataAccessEntity updateAccessEntity = new DataAccessEntity();
        updateAccessEntity.setType(DataAccess.Type.OWN_CREATED.name());
        updateAccessEntity.setAction(Permission.ACTION_UPDATE);
        //脚本方式自定义控制
//        updateAccessEntity.setConfig(JSON.toJSONString(new SimpleScriptDataAccess("" +
//                "println(id);" +
//                "println(entity);" +
//                "println('脚本权限控制');" +
//                "return true;" +
//                "","groovy")));

        //password 属性不能读取和修改
        FieldAccessEntity fieldAccessEntity = new FieldAccessEntity();
        fieldAccessEntity.setField("password");
        fieldAccessEntity.setActions(ActionEntity.create(Permission.ACTION_QUERY, Permission.ACTION_UPDATE));

        PermissionEntity permission = entityFactory.newInstance(PermissionEntity.class);
        permission.setName("测试");
        permission.setId("test");
        permission.setStatus((byte) 1);
        permission.setActions(ActionEntity.create(Permission.ACTION_QUERY, Permission.ACTION_UPDATE));
        permission.setDataAccess(Arrays.asList(accessEntity, updateAccessEntity));
        permission.setFieldAccess(Arrays.asList(fieldAccessEntity));
        permissionService.insert(permission);

        BindPermissionRoleEntity<PermissionRoleEntity> roleEntity = entityFactory.newInstance(BindPermissionRoleEntity.class);
        SimplePermissionRoleEntity permissionRoleEntity = new SimplePermissionRoleEntity();
        permissionRoleEntity.setRoleId("admin");
        permissionRoleEntity.setPermissionId("test");
        permissionRoleEntity.setActions(Arrays.asList(Permission.ACTION_QUERY, Permission.ACTION_UPDATE));
        permissionRoleEntity.setDataAccesses(permission.getDataAccess());
        permissionRoleEntity.setFieldAccesses(permission.getFieldAccess());
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
        clientEntity.setSupportGrantType(Arrays.asList("*"));
        oAuth2ClientDao.insert(clientEntity);
    }
}
