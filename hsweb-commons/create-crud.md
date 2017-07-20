# 使用通用CRUD 创建 dao,service,controller...


hsweb 按照功能分模块, 再将controller,service,dao等分为子模块.
以[hsweb-system-menu](../hsweb-system/hsweb-system-menu)为例,创建maven项目模块以及子模块.
## 模块结构
* hsweb-system-menu
    - hsweb-system-menu-controller
    - hsweb-system-menu-dao
        - hsweb-system-menu-dao-api
        - hsweb-system-menu-dao-mybatis
    - hsweb-system-menu-entity
    - hsweb-system-menu-model
    - hsweb-system-menu-service
         - hsweb-system-menu-service-api
         - hsweb-system-menu-service-simple
         - hsweb-system-menu-service-cloud
    - hsweb-system-menu-starter

[使用idea创建时的常见问题](https://github.com/hs-web/hsweb-framework/issues/31)

## Entity 
模块:hsweb-system-menu-entity

    hsweb中的entity都为接口并提供了一个默认实现,例如 MenuEntity=>SimpleMenuEntity.
    但是并不强制使用此方式创建entity. 可以只有类,不使用接口.
约定: 
1. entity应该实现`Entity`接口
2. 有主键的entity应该实现`GenericEntity<PK>`接口
3. entity应该使用`EntityFactory`创建而不是new
4. 树形结构的entity,可以实现`TreeSortSupportEntity<PK>`

注: `PK`=主键

创建一个entity.

1. 引入maven依赖
```xml
 <dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-commons-entity</artifactId>
    <version>${hsweb.version}</version>
</dependency>
```
2. 新建接口类:
```java
package org.hswebframework.web.entity.menu;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;

public interface MenuEntity extends GenericEntity<String> {

    String getName();
    
    void setName(String remark);
    
    String getUrl();
        
    void setUrl(String url);
}

```

3. 新建默认实现类

```java
package org.hswebframework.web.entity.menu;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;

public class SimpleMenuEntity implements MenuEntity {
    private String name;
    private String url;
    
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getUrl(){
        return this.url;
    }
    public void setUrl(String url){
        this.url=url;
    }
}

```

注意: 默认实现类一般和接口在同一个包中,并且名称为Simple开头+接口名称.
因为默认的`EntityFactory`按照此约定来创建未指定特殊实现接口实现的实例.详见 [MapperEntityFactory](hsweb-commons-entity/src/main/java/org/hswebframework/web/commons/entity/factory/MapperEntityFactory.java)

## DAO
模块:hsweb-system-menu-dao

    hsweb 目前提供了mybatis的通用dao实现,支持动态条件.
常用dao接口:
1. [InsertDao](hsweb-commons-dao/hsweb-commons-dao-api/src/main/java/org/hswebframework/web/dao/InsertDao.java) : 支持insert
2. [DeleteDao](hsweb-commons-dao/hsweb-commons-dao-api/src/main/java/org/hswebframework/web/dao/DeleteDao.java) : 支持根据主键删除
3. [DeleteByEntityDao](hsweb-commons-dao/hsweb-commons-dao-api/src/main/java/org/hswebframework/web/dao/dynamic/DeleteByEntityDao.java) : 支持根据实体删除（动态条件）
4. [QueryByEntityDao](hsweb-commons-dao/hsweb-commons-dao-api/src/main/java/org/hswebframework/web/dao/dynamic/QueryByEntityDao.java) : 支持根据实体查询（动态条件）
5. [UpdateByEntityDao](hsweb-commons-dao/hsweb-commons-dao-api/src/main/java/org/hswebframework/web/dao/dynamic/UpdateByEntityDao.java) : 支持根据实体更新（动态条件）
6. [CrudDao](hsweb-commons-dao/hsweb-commons-dao-api/src/main/java/org/hswebframework/web/dao/CrudDao.java) : 集上述dao于一体

增删改查功能继承 `CrudDao`即可.

1. 新建Dao接口
进入模块: hsweb-system-menu-dao-api 引入依赖
```xml
 <dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-commons-dao-api</artifactId>
    <version>${hsweb.version}</version>
</dependency>
```
创建接口:
```java
package org.hswebframework.web.dao.menu;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.entity.menu.MenuEntity;

public interface MenuDao extends CrudDao<MenuEntity, String> {
}

```
2. mybatis实现.
进入模块: hsweb-system-menu-dao-mybatis引入依赖
```xml
 <dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-commons-dao-mybatis</artifactId>
    <version>${hsweb.version}</version>
</dependency>
```
        hsweb依然使用xml的方式实现dao,xml建议放到resources目录下如: 'resources/org/hswebframework/web/dao/mybatis/mappers/menu'

```xml
<?xml version="1.0" encoding="UTF-8" ?>

<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://www.mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="org.hswebframework.web.dao.menu.MenuDao">
    <resultMap id="MenuResultMap" type="org.hswebframework.web.entity.menu.SimpleMenuEntity">
        <id property="id" column="u_id" javaType="string" jdbcType="VARCHAR"/>
        <result property="name" column="name" javaType="String" jdbcType="VARCHAR"/>
        <result property="url" column="url" javaType="String" jdbcType="VARCHAR"/>
    </resultMap>

    <!--用于动态生成sql所需的配置-->
    <sql id="config">
        <bind name="resultMapId" value="'MenuResultMap'"/>
        <bind name="tableName" value="'s_menu'"/>
    </sql>

    <insert id="insert" parameterType="org.hswebframework.web.commons.entity.Entity">
        <include refid="config"/>
        <include refid="BasicMapper.buildInsertSql"/>
    </insert>

    <delete id="deleteByPk" parameterType="String">
        delete from s_menu where u_id =#{id}
    </delete>

    <delete id="delete" parameterType="org.hswebframework.web.commons.entity.Entity">
        <include refid="config"/>
        <include refid="BasicMapper.buildDeleteSql"/>
    </delete>

    <update id="update" parameterType="org.hswebframework.web.commons.entity.Entity">
        <include refid="config"/>
        <include refid="BasicMapper.buildUpdateSql"/>
    </update>

    <select id="query" parameterType="org.hswebframework.web.commons.entity.Entity" resultMap="MenuResultMap">
        <include refid="config"/>
        <include refid="BasicMapper.buildSelectSql"/>
    </select>

    <select id="count" parameterType="org.hswebframework.web.commons.entity.Entity" resultType="int">
        <include refid="config"/>
        <include refid="BasicMapper.buildTotalSql"/>
    </select>
</mapper>

```

注意: 目前动态条件参数仅支持: `QueryParamEntity`,`UpdateParamEntity`,`DeleteParamEntity`


## Service
模块: hsweb-system-menu-service

    通用service中,很多实现使用接口(java8的default),以实现多继承

常用通用service接口:
1. [InsertService](hsweb-commons-service/hsweb-commons-service-api/src/main/java/org/hswebframework/web/service/InsertService.java):增
2. [DeleteService](hsweb-commons-service/hsweb-commons-service-api/src/main/java/org/hswebframework/web/service/DeleteService.java):删
3. [UpdateService](hsweb-commons-service/hsweb-commons-service-api/src/main/java/org/hswebframework/web/service/UpdateService.java):改
4. [QueryService](hsweb-commons-service/hsweb-commons-service-api/src/main/java/org/hswebframework/web/service/QueryService.java):查
5. [QueryByEntityService](hsweb-commons-service/hsweb-commons-service-api/src/main/java/org/hswebframework/web/service/QueryByEntityService.java):动态查
6. [CrudService](hsweb-commons-service/hsweb-commons-service-api/src/main/java/org/hswebframework/web/service/CrudService.java): 合以上为一
7. [TreeService](hsweb-commons-service/hsweb-commons-service-api/src/main/java/org/hswebframework/web/service/TreeService.java):树结构(`TreeSupportEntity`)常用操作服务

常用通用service实现:
1. [GenericService](hsweb-commons-service/hsweb-commons-service-simple/src/main/java/org/hswebframework/web/service/GenericService.java): 通用服务,提供增删改查,dsl方式操作接口.
2. [AbstractService](hsweb-commons-service/hsweb-commons-service-simple/src/main/java/org/hswebframework/web/service/AbstractService.java):提供验证器等服务类常用操作,实现`CreateEntityService`.
3. [AbstractTreeSortService](hsweb-commons-service/hsweb-commons-service-simple/src/main/java/org/hswebframework/web/service/AbstractTreeSortService.java):同上,对树形结构操作.实现`TreeService`.
4. [GenericEntityService](hsweb-commons-service/hsweb-commons-service-simple/src/main/java/org/hswebframework/web/service/GenericEntityService.java): 通用服务,实现对`GenericEntity`的增删改查操作
5. [DefaultDSLDeleteService](hsweb-commons-service/hsweb-commons-service-simple/src/main/java/org/hswebframework/web/service/DefaultDSLDeleteService.java): dsl方式删除
6. [DefaultDSLQueryService](hsweb-commons-service/hsweb-commons-service-simple/src/main/java/org/hswebframework/web/service/DefaultDSLQueryService.java): dsl方式查询
7. [DefaultDSLUpdateService](hsweb-commons-service/hsweb-commons-service-simple/src/main/java/org/hswebframework/web/service/DefaultDSLUpdateService.java): dsl方式更新

DSL方式操作使用[easy-orm](https://github.com/hs-web/hsweb-easy-orm)来构建动态查询参数,[使用方法](hsweb-commons-service/hsweb-commons-service-simple/README.md).

1. 创建service接口
进入模块: hsweb-system-menu-service-api

引入依赖:
```xml
 <dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-commons-service-api</artifactId>
    <version>${hsweb.version}</version>
</dependency>
```

创建接口类:
```java
package org.hswebframework.web.service.menu;


import org.hswebframework.web.entity.menu.MenuEntity;
import org.hswebframework.web.service.CrudService;

import java.util.List;

public interface MenuService
//泛型<实体类型,主键类型>
        extends CrudService<MenuEntity, String> {
    
}

```

进入模块:hsweb-system-menu-service-simple

引入依赖:
```xml
<!--上面创建的service接口模块-->
 <dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-system-menu-service-api</artifactId>
    <version>${hsweb.version}</version>
</dependency>
<!--通用service实现模块-->
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-commons-service-simple</artifactId>
    <version>${hsweb.version}</version>
</dependency>
```
创建实现类
```java
package org.hswebframework.web.service.menu.simple;

import org.hswebframework.web.dao.menu.MenuDao;
import org.hswebframework.web.entity.menu.MenuEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.menu.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("menuService")
public class SimpleMenuService
        //泛型<实体类型,主键类型>
        extends GenericEntityService<MenuEntity, String>
        implements MenuService {

    private MenuDao menuDao;
    
    //ID生成器,通用服务的ID都使用主动ID生成,不使用orm或者数据库自动生成
    //可通过自己实现IDGenerator进行自定义生成
    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    // 实现CrudDao接口的类
    @Override
    public MenuDao getDao() {
        return menuDao;
    }
    
    //注入dao实现
    @Autowired
    public void setMenuDao(MenuDao menuDao) {
        this.menuDao = menuDao;
    }
}

```

## controller
模块: hsweb-system-menu-controller

常用通用controller接口

1. [CreateController](hsweb-commons-controller/src/main/java/org/hswebframework/web/controller/CreateController.java) : 增
2. [DeleteController](hsweb-commons-controller/src/main/java/org/hswebframework/web/controller/DeleteController.java) : 删
3. [UpdateController](hsweb-commons-controller/src/main/java/org/hswebframework/web/controller/UpdateController.java) : 改
4. [QueryController](hsweb-commons-controller/src/main/java/org/hswebframework/web/controller/QueryController.java) : 查
5. [CrudController](hsweb-commons-controller/src/main/java/org/hswebframework/web/controller/CrudController.java) : 增删改查

泛型: E, PK, Q extends Entity, M => Entity,主键,动态查询实体类,Model. 
增改时,使用Model接收参数;查询时,使用Q接受参数,使用Model作为响应.
注意: Model 并不是必须,如果不使用单独的Model,可使用 `SimpleCrudController`. 通用controller使用restful方式提供接口
响应结果统一为`ResponseMessage<T>`. [更多使用方法](hsweb-commons-controller/README.md)

1. 创建Controller

进入模块:hsweb-system-menu-controller

引入依赖:
```xml
<!--只依赖service接口-->
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-system-menu-service-api</artifactId>
    <version>${hsweb.version}</version>
</dependency>
<!--通用Controller-->
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-commons-controller</artifactId>
    <version>${hsweb.version}</version>
</dependency>
```
创建类:
```java
package org.hswebframework.web.controller.menu;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.GenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.menu.MenuEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.menu.MenuGroupService;
import org.hswebframework.web.service.menu.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

@RestController
@RequestMapping("${hsweb.web.mappings.menu:menu}") //默认/menu
@Authorize(permission = "menu") // menu权限
@Api(value = "menu-manager", description = "系统菜单管理") //swagger
public class MenuController implements
//泛型 <实体,主键,动态查询实体(目前只支持此类型),模型>
//等同 SimpleGenericEntityController<MenuEntity, String, QueryParamEntity>
 GenericEntityController<MenuEntity, String, QueryParamEntity, MenuEntity> {

    private MenuService menuService;

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public MenuService getService() {
        return menuService;
    }
}
```

## starter
模块: hsweb-system-menu-starter
模块整合,自动配置.

1. 引入依赖
```xml
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-system-menu-service-simple</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-system-menu-dao-mybatis</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-system-menu-controller</artifactId>
    <version>${project.version}</version>
</dependency>

<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-spring-boot-starter</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-tests</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>

<!--其他依赖-->
```

2. 新建文件: `resources/hsweb-starter.js`,此脚本在模块第一次使用或者更新版本的时候被执行
内容如下:

```js
//组件信息
var info = {
    groupId: "org.hsweb",
    artifactId: "hsweb-system-menu",
    version: "3.0",
    website: "https://github.com/hs-web/hsweb-framework/tree/master/hsweb-system/hsweb-system-menu",
    author: "zh.sqy@qq.com",
    comment: "菜单"
};

//版本更新信息
var versions = [
    // {
    //     version: "3.0.1",
    //     upgrade: function (context) {
    //          //如果已安装3.0.0，准备使用3.0.1，将执行此代码
    //         java.lang.System.out.println("更新到3.0.2了");
    //     }
    // }
];
var JDBCType = java.sql.JDBCType;
function install(context) {
    //当首次使用此模块的时候,执行创建数据库
    var database = context.database;
    database.createOrAlter("s_menu")
        .addColumn().name("u_id").varchar(32).notNull().primaryKey().comment("uid").commit()
        .addColumn().name("name").varchar(64).notNull().comment("名称").commit()
        .addColumn().name("url").varchar(2048).notNull().comment("url").commit()
        //更多字段
        //。。。
        .comment("系统菜单表").commit()
}

//以下为固定写法,无需改动
dependency.setup(info)
    .onInstall(install)
    .onUpgrade(function (context) { //更新时执行
        var upgrader = context.upgrader;
        upgrader.filter(versions)
            .upgrade(function (newVer) {
                newVer.upgrade(context);
            });
    })
    .onUninstall(function (context) { 
        //卸载时执行
    });
```

3. 自动配置类
目前无需创建自动配置类
    
4. 单元测试
TODO

在使用的时候,直接依赖starter即可:
```xml
 <dependency>
     <groupId>org.hswebframework.web</groupId>
     <artifactId>hsweb-system-menu-starter</artifactId>
     <version>${project.version}</version>
 </dependency>
```
在未来将提供更多的starter,例如dubbo,spring-cloud