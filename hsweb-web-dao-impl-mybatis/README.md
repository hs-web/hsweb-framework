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
        <version>1.0.1-SNAPSHOT</version>
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
            <id property="id" column="u_id" javaType="string" jdbcType="VARCHAR"/>
            <result property="username" column="username" javaType="String" jdbcType="VARCHAR"/>
            <result property="password" column="password" javaType="String" jdbcType="VARCHAR"/>
            <result property="name" column="name" javaType="String" jdbcType="VARCHAR"/>
            <result property="email" column="email" javaType="String" jdbcType="VARCHAR"/>
            <result property="phone" column="phone" javaType="String" jdbcType="VARCHAR"/>
            <result property="status" column="status" javaType="int" jdbcType="INTEGER"/>
            <result property="createDate" column="create_date" javaType="java.util.Date" jdbcType="TIMESTAMP"/>
            <result property="updateDate" column="update_date" javaType="java.util.Date" jdbcType="TIMESTAMP"/>
        </resultMap>
        <!--用于动态生成sql所需的配置-->
        <sql id="config">
            <bind name="resultMapId" value="'UserResultMap'"/>
            <bind name="tableName" value="'s_user'"/>
        </sql>
    
        <insert id="insert" parameterType="org.hsweb.web.bean.common.InsertParam" >
            <include refid="config"/>
            <include refid="BasicMapper.buildInsertSql"/>
        </insert>
    
        <delete id="delete" parameterType="org.hsweb.web.bean.common.DeleteParam">
           <include refid="config"/>
           <include refid="BasicMapper.buildDeleteSql"/>
        </delete>
    
        <update id="updatePassword" parameterType="User">
            update s_user set password=#{password,jdbcType=VARCHAR} where u_id = #{id}
        </update>
    
        <update id="update" parameterType="org.hsweb.web.bean.common.UpdateParam">
            <include refid="config"/>
            <include refid="BasicMapper.buildUpdateSql"/>
        </update>
    
        <select id="selectByUserName" parameterType="string" resultMap="UserResultMap">
            select * from s_user where username=#{username}
        </select>
    
        <select id="selectByPk" parameterType="string" resultMap="UserResultMap">
            select * from s_user where u_id=#{u_id}
        </select>
    
        <select id="select" parameterType="org.hsweb.web.bean.common.QueryParam" resultMap="UserResultMap">
            <include refid="config"/>
            <include refid="BasicMapper.buildSelectSql"/>
        </select>
    
        <select id="total" parameterType="org.hsweb.web.bean.common.QueryParam" resultType="int">
            <include refid="config"/>
            <include refid="BasicMapper.buildTotalSql"/>
        </select>
    </mapper>
```
