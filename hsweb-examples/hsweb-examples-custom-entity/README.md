# 拓展实体演示
想拓展系统自带功能的实体?如添加字段.

测试: 运行`org.hswebframework.web.example.custom.Application`

提交数据:
```bash
curl -l -H "Content-type: application/json" \
-X POST -d '{"name":"旧的属性","nameEn":"拓展的属性"}' \
http://localhost:8081/organizational
```
成功返回:
```json
{"result":"fd13ec65130d5ed66491a1e0453a3172","status":200,"timestamp":1497678000068}
```

获取数据:
```bash
curl http://localhost:8081/organizational/fd13ec65130d5ed66491a1e0453a3172
```
可以看到数据已经有新的字段
# 实体类
1. 编写实体类,继承需要拓展的实体,如:
```java
package org.hswebframework.web.example.custom.entity;

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

2. 提供给hsweb
将新的实体类提供给hsweb有3种方式,第一种:jdk的serviceLoader;第二种:application.yml配置;
第三种:java类方式配置,选择其中一种即可.

serviceLoader

创建文件:`META-INF/services/org.hswebframework.web.entity.organizational.OrganizationalEntity`
内容:
```text
org.hswebframework.web.example.custom.entity.CustomOrganizationalEntity
```

application.yml
```yaml
hsweb: 
   entity:
     mappings:
         -  source-base-package: org.hswebframework.web.entity.organizational
            target-base-package: org.hswebframework.web.example.custom.entity
            mapping:
                OrganizationalEntity: CustomOrganizationalEntity
```

java类
```java
    package org.hswebframework.web.example.custom.config;
    
    import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
    import org.hswebframework.web.entity.organizational.OrganizationalEntity;
    import org.hswebframework.web.example.custom.entity.CustomOrganizationalEntity;
    import org.hswebframework.web.starter.entity.EntityMappingCustomer;
    import org.springframework.stereotype.Component;
    
    /**
     * 自定义实体关系
     *
     * @author zhouhao
     * @since 3.0
     */
    @Component
    public class CustomEntityMappingCustomer implements EntityMappingCustomer {
        @Override
        public void customize(MapperEntityFactory entityFactory) {
            //OrganizationalEntity使用CustomOrganizationalEntity实现
            entityFactory.addMapping(OrganizationalEntity.class,
                    MapperEntityFactory.defaultMapper(CustomOrganizationalEntity.class));
        }
    }

```

3. 编写新的mybatis mapper配置

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://www.mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.hswebframework.web.dao.organizational.OrganizationalDao">
     <!--修改type为新的实体类型-->
    <resultMap id="OrganizationalResultMap" type="org.hswebframework.web.example.custom.entity.CustomOrganizationalEntity">
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
        <!--拓展的属性-->
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
    <insert id="insert" parameterType="org.hswebframework.web.example.custom.entity.CustomOrganizationalEntity">
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

4. 覆盖mybatis mapper xml配置
覆盖方式有2种: application.yml或者java类配置.选择其一即可.

application.yml

```yaml
mybatis:
  mapper-location-excludes: classpath*:org/hswebframework/**/OrganizationalMapper.xml
  mapper-locations: classpath*:custom/mappers/OrganizationalMapper.xml
```

java class
```java
package org.hswebframework.web.example.custom.config;

import org.hswebframework.web.dao.mybatis.MybatisMapperCustomer;
import org.springframework.stereotype.Component;

@Component
public class CustomMybatisMapperCustomer implements MybatisMapperCustomer {
    @Override
    public String[] getExcludes() {
        return new String[]{
                "classpath*:org/hswebframework/**/OrganizationalMapper.xml"
        };
    }

    @Override
    public String[] getIncludes() {
        return new String[]{
                "classpath*:custom/mappers/OrganizationalMapper.xml"
        };
    }
}
```
