# 通用增删改查
hsweb 中提供了一套通用的增删改查封装([hsweb-commons](../../../hsweb-commons)),实现增删改查以及动态查询。

接口约定: 
1. 实体类需要实现`Entity`接口, 通用实体类继承`GenericEntity<主键>`类.
2. Dao继承`Dao`接口, 通用增删改查继承`CrudDao<实体类,主键>`.
3. Service继承`Service`接口,通用增删改查继承`CrudService`.
4. Controller,通用增删改查实现`SimpleGenericEntityController<实体类,主键,动态查询参数>`

实现约定:
1. 框架提供的实体都是接口形式,使用`EntityFactory`来创建实例,便于拓展属性. 实际业务中,可能并不需要这么做.
2. Dao通用增删改查目前提供mybatis实现,可参照[UserMapper.xml](https://github.com/hs-web/hsweb-framework/blob/master/hsweb-system/hsweb-system-authorization/hsweb-system-authorization-dao/hsweb-system-authorization-dao-mybatis/src/main/resources/org/hswebframework/web/dao/mybatis/mappers/authorization/UserMapper.xml#L23-L69)
   使用xml方式,提供了动态条件的同时保留了灵活性.
3. Service提供了dsl的方式构造动态条件,继承`GenericEntityService<实体类,主键>`即可.注意:框架未使用dao来生成主键,而是在service中通过IDGenerator来生成.

# 动态条件

### Service中使用dsl进行动态条件
继承`GenericEntityService`后可获得dsl方式动态条件功能:

```java
    public void method(){
       //where name=? or name = ?
       createQuery().where("name","张三").or("name","李四").list();
       //set status=? where id = ?
       createUpdate().set("status",1).where("id",id).exec(); //注意需要调用exec()
    }
```
更多用法,详见:[hsweb-commons-service-simple](https://github.com/hs-web/hsweb-framework/tree/master/hsweb-commons/hsweb-commons-service/hsweb-commons-service-simple)

### 前端传参,动态查询
目前仅实现了一种动态条件从参数:`QueryParamEntity`,
因此Controller实现`SimpleGenericEntityController<实体类,主键,QueryParamEntity>`接口. 获得动态查询功能

```bash
  GET /user?terms[0].column=name&terms[0].termType=like&terms[0].value=张三
  // 等同于 where name like ?
```
更多用法,详见:[hsweb-commons-controller](https://github.com/hs-web/hsweb-framework/tree/master/hsweb-commons/hsweb-commons-controller)

# 表关联
由于动态条件实现较简单,目前动态条件需要修改mybatis dao实现的的mapper.xml,局部代码如下
```xml
 <resultMap id="CardDataResultMap" type="com.zmcsoft.apsp.api.card.entity.SimpleCardDataEntity">
        <id property="id" column="u_id" javaType="string" jdbcType="VARCHAR"/>
        <result property="name" column="name" javaType="String" jdbcType="VARCHAR"/>
        
        <!--关联表的信息-->
        <result property="detail.email" column="detail.email" javaType="String" jdbcType="VARCHAR"/>
        <result property="detail.phoneNumber" column="detail.phone_number" javaType="String" jdbcType="VARCHAR"/>
</resultMap>

 <select id="query" parameterType="org.hswebframework.web.commons.entity.Entity" resultMap="CardDataResultMap">
        <include refid="config"/>
        select
        <include refid="BasicMapper.buildSelectField"/>
        from user 
        left join user_detail detail on detail.user_id = user.id
        <where>
            <include refid="BasicMapper.buildWhere"/>
        </where>
        <include refid="BasicMapper.buildSortField"/>
</select>

<select id="count" parameterType="org.hswebframework.web.commons.entity.Entity" resultMap="CardDataResultMap">
        <include refid="config"/>
        select count(1) from user 
        left join user_detail detail on detail.user_id = user.id
        <where>
            <include refid="BasicMapper.buildWhere"/>
        </where>
        <include refid="BasicMapper.buildSortField"/>
</select>
```

然后就可以通过动态查询来查询了
```java
createQuery().where("detail.email","admin@hsweb.me").single(); 
```