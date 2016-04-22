# mybatis dao实现
主要功能： 

1. 实现各个功能dao
2. 实现通用mapper
3. 通用mapper 支持自动生成多种查询条件如: LIKE,IN,GT,LT等等,使用方式:查询条件名后追加关键字$,如: name$LIKE,id$IN

# 使用
```xml
     <dependency>
        <groupId>org.hsweb</groupId>
        <artifactId>hsweb-web-dao-impl-mybatis</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```

# 配置参考
```xml
    <?xml version="1.0" encoding="UTF-8" ?>
    <!DOCTYPE mapper
            PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
            "http://www.mybatis.org/dtd/mybatis-3-mapper.dtd">
    
    <mapper namespace="org.hsweb.web.dao.user.UserMapper">
        <resultMap id="UserResultMap" type="User">
            <id property="u_id" column="u_id" javaType="string" jdbcType="VARCHAR"/>
            <!--***********-->
        </resultMap>
        <!--字段信息配置-->
        <sql id="fieldConfig">
            <bind name="$fieldsInfo"
                  value="#{
                        'u_id':#{'jdbcType':'VARCHAR','javaType':'string'}
                        ,'username':#{'jdbcType':'VARCHAR','javaType':'string'}
                        ,'password':#{'jdbcType':'VARCHAR','javaType':'string'}
                        ,'name':#{'jdbcType':'VARCHAR','javaType':'string'}
                        ,'email':#{'jdbcType':'VARCHAR','javaType':'string'}
                        ,'phone':#{'jdbcType':'VARCHAR','javaType':'string'}
                        ,'status':#{'jdbcType':'INTEGER','javaType':'number'}
                        ,'create_date':#{'jdbcType':'TIMESTAMP','javaType':'date'}
                        ,'update_date':#{'jdbcType':'TIMESTAMP','javaType':'date'}
                        }"/>
            <bind name="$fields" value="$fieldsInfo.keySet()"/>
        </sql>
        <!--表名-->
        <sql id="tableName">
            <bind name="$tableName" value="'s_user'"/>
        </sql>
    
        <insert id="insert" parameterType="User" useGeneratedKeys="true" keyProperty="data.u_id" keyColumn="U_ID">
            <include refid="fieldConfig"/>
            <include refid="tableName"/>
            <include refid="BasicMapper.buildInsertSql"/>
        </insert>
    
        <delete id="delete" parameterType="UserRole">
            delete from s_user where u_id=#{u_id}
        </delete>
    
        <update id="update" parameterType="org.hsweb.web.bean.common.UpdateParam">
            <include refid="fieldConfig"/>
            <include refid="tableName"/>
            <include refid="BasicMapper.buildUpdateSql"/>
        </update>
        
        <select id="selectByPk" parameterType="string" resultMap="UserResultMap">
            select * from s_user where u_id=#{u_id}
        </select>
    
        <select id="select" parameterType="org.hsweb.web.bean.common.QueryParam" resultMap="UserResultMap">
            <include refid="fieldConfig"/>
            <include refid="tableName"/>
            <include refid="BasicMapper.buildSelectSql"/>
        </select>
    
        <select id="total" parameterType="org.hsweb.web.bean.common.QueryParam" resultType="int">
            <include refid="fieldConfig"/>
            <include refid="tableName"/>
            <include refid="BasicMapper.buildTotalSql"/>
        </select>
    </mapper>
```
