# 拓展自定义字段

在`hsweb-system`中提供了一些业务功能,但是有的功能只提供了基本的字段信息.`hsweb`提供了拓展字段而无需修改框架源码的方法.

## 拓展实体类

以拓展组织架构中的组织字段为例

1. 编写实体类,继承需要拓展的实体:
```java
package com.myproject.entity;

import org.hswebframework.web.entity.organizational.SimpleOrganizationalEntity;

public class CustomOrganizationalEntity extends SimpleOrganizationalEntity {
    
    /**********拓展字段**********/
    private String leader;

    private String nameEn;

    private String otherProperty;

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getOtherProperty() {
        return otherProperty;
    }

    public void setOtherProperty(String otherProperty) {
        this.otherProperty = otherProperty;
    }
}
```

2. 告诉`hsweb`使用新的实体类

将新的实体类提供给hsweb有3种方式.

    第一种:java自带的serviceLoader;
    第二种:application.yml配置;
    第三种:java类方式配置.
    注意: 选择其中任意一种即可.

#### serviceLoader方式

创建文件:`META-INF/services/org.hswebframework.web.entity.organizational.OrganizationalEntity`内容:

```text
com.myproject.entity.CustomOrganizationalEntity
```

#### application.yml方式

```yaml
hsweb: 
   entity:
     mappings:
         -  source-base-package: org.hswebframework.web.entity.organizational
            target-base-package: com.myproject.entity
            mapping:
                OrganizationalEntity: CustomOrganizationalEntity
```

#### java类方式
```java
    @Component
    public class CustomEntityMappingCustomizer implements EntityMappingCustomizer {
        @Override
        public void customize(MapperEntityFactory entityFactory) {
            //OrganizationalEntity使用CustomOrganizationalEntity实现
            entityFactory.addMapping(OrganizationalEntity.class,
                    MapperEntityFactory.defaultMapper(CustomOrganizationalEntity.class));
        }
    }

```

##  修改Dao字段映射

使用mybatis作为dao实现时,如果实体类上没有使用jpa注解则需要修改`mapper.xml`的配置来拓展字段.

jpa注解和mapper配置各有优势(jpa更简单,但只支持简单的字段.mybatis配置稍微复杂,灵活性更高),请根据实际情况选择合适的方式.

#### 修改mapper配置文件方式

1. 创建mapper.xml,可直接复制旧的xml进行修改.旧的xml可在`hsweb-system`中对应的模块进行查找.

`com/myproject/mappers/OrganizationalMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://www.mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.hswebframework.web.dao.organizational.OrganizationalDao">
    <resultMap id="OrganizationalResultMap" type="org.hswebframework.web.entity.organizational.OrganizationalEntity">
        <!--默认的属性-->
        <id property="id" column="u_id" javaType="string" jdbcType="VARCHAR"/>
        <result property="name" column="name" javaType="String" jdbcType="VARCHAR"/>
        <result property="fullName" column="full_name" javaType="String" jdbcType="VARCHAR"/>
        <result property="code" column="code" javaType="String" jdbcType="VARCHAR"/>
        <result property="optionalRoles" column="optional_roles" javaType="java.util.List" jdbcType="CLOB"/>
        <result property="parentId" column="parent_id" javaType="String" jdbcType="VARCHAR"/>
        <result property="path" column="path" javaType="String" jdbcType="VARCHAR"/>
        <result property="sortIndex" column="sort_index" javaType="Long" jdbcType="DECIMAL"/>
        <result property="status" column="status" javaType="Byte" jdbcType="DECIMAL"/>
        <result property="level" column="level" javaType="Integer" jdbcType="DECIMAL"/>
        <!--重点: 拓展的属性-->
        <result property="nameEn" column="name_en" javaType="String" jdbcType="VARCHAR"/>
        <result property="leader" column="leader" javaType="String" jdbcType="VARCHAR"/>
        <result property="otherProperty" column="other_property" javaType="String" jdbcType="VARCHAR"/>

    </resultMap>

    <!--用于动态生成sql所需的配置-->
    <sql id="config">
        <bind name="resultMapId" value="'OrganizationalResultMap'"/>
        <bind name="tableName" value="'s_organization'"/>
    </sql>
    <!--修改parameterType为新的实体类型-->
    <insert id="insert" parameterType="com.myproject.entity.CustomOrganizationalEntity">
        <include refid="config"/>
        <include refid="BasicMapper.buildInsertSql"/>
    </insert>

    <delete id="deleteByPk" parameterType="String">
        delete from s_organization where u_id =#{id}
    </delete>

    <delete id="delete" parameterType="org.hswebframework.web.commons.entity.Entity">
        <include refid="config"/>
        <include refid="BasicMapper.buildDeleteSql"/>
    </delete>

    <update id="update" parameterType="org.hswebframework.web.commons.entity.Entity">
        <include refid="config"/>
        <include refid="BasicMapper.buildUpdateSql"/>
    </update>

    <select id="query" parameterType="org.hswebframework.web.commons.entity.Entity" resultMap="OrganizationalResultMap">
        <include refid="config"/>
        <include refid="BasicMapper.buildSelectSql"/>
    </select>

    <select id="count" parameterType="org.hswebframework.web.commons.entity.Entity" resultType="int">
        <include refid="config"/>
        <include refid="BasicMapper.buildTotalSql"/>
    </select>
</mapper>
```

2. 覆盖`mapper.xml`配置,将`hsweb`自带的配置替换为新的配置.

        覆盖方式有2种: application.yml或者java类配置.选择其一即可.

application.yml方式:

```yaml
mybatis:
  mapper-location-excludes: classpath*:org/hswebframework/**/OrganizationalMapper.xml #不加载的xml
  mapper-locations: classpath*:com/myproject/mappers/OrganizationalMapper.xml
```

java类配置方式:
```java
@Component //提供给spring才会生效
public class CustomMybatisMapperCustomizer implements MybatisMapperCustomizer {
    @Override
    public String[] getExcludes() {
        return new String[]{
                "classpath*:org/hswebframework/**/OrganizationalMapper.xml"
        };
    }

    @Override
    public String[] getIncludes() {
        return new String[]{
                "classpath*:com/myproject/mappers/OrganizationalMapper.xml"
        };
    }
}
```

#### 使用jpa注解方式

依赖jpa-api:
```xml
<dependency>
    <groupId>org.hibernate.javax.persistence</groupId>
    <artifactId>hibernate-jpa-2.0-api</artifactId>
    <version>1.0.1.Final</version>
</dependency>
```

在拓展的实体类中使用jpa注解:
```java
    @Data
    @Table //此处设置表名是无效的,仅作为一个解析标识
    public class CustomUserEntity extends SimpleBindRoleUserEntity {
        @Column(name = "nick_name")
        private String nickName;
    }
```
注意： 暂时只支持简单的属性。不支持表关联

