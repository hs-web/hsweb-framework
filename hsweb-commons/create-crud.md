# 使用通用CRUD 创建 dao,service,controller...

## 模块结构
hsweb 按照功能分模块, 再将controller,service,dao分为子模块.例如:
模块名称为 hsweb-system-menu
创建maven项目模块以及子模块如下:

* hsweb-system-menu
    - hsweb-system-menu-controller
    - hsweb-system-menu-dao
        - hsweb-system-menu-dao-api
        - hsweb-system-menu-dao-mybatis
    - hsweb-system-menu-entity
    - hsweb-system-menu-service
         - hsweb-system-menu-service-api
         - hsweb-system-menu-service-simple
         - hsweb-system-menu-service-cloud
    - hsweb-system-menu-starter

[使用idea创建时的常见问题](https://github.com/hs-web/hsweb-framework/issues/31)

## Entity 
模块:hsweb-system-menu-entity

    hsweb中的entity都为接口并提供了一个默认实现,例如 `MenuEntity`=>`SimpleMenuEntity`.但是并不强制使用此方式创建entity
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

    一般增删改查功能继承 `CrudDao`即可.

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
1. mybatis实现.
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
通用service 提供dsl方式的删改查

TODO