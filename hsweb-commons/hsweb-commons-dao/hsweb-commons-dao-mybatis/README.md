# 基于mybatis的通用crud实现

使用myabtis和easy-orm对`hsweb-commons-dao-api`进行了实现,提供动态条件对crud支持.

# 使用
在pom.xml中引入:

```xml
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-commons-dao-mybatis</artifactId>
    <version>${hsweb.framework.version}</version>
</dependency>
```

# 配置
application.yml

```yaml
mybatis:
  # 扫描myabtis mapper xml的路径
  mapper-locations: classpath*:com/company/app/**/*Mapper.xml
  # 这里需要配置扫描枚举,才能支持对实现了EnumDict接口的枚举进行序列化和反序列化
  type-handlers-package: com.company.app.enums 
  # 是否开启动态数据源,开启后才能支持在同一个dao中切换数据源
  dynamic-datasource: false
  # 排除扫描xml配置,用于需要拓展无法修改的mapper xml时,通过此配置不加载对应的xml,然后通过mapper-locations配置加载新的xml.
  mapper-location-excludes: classpath*:com/company/app/x/y/*Mapper.xml
```

# 使用通用Mapper XMl

目前仅支持xml的方式,例如:

```xml

<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="org.hswebframework.web.dao.crud.TestDao">

    <resultMap id="TestResultMap" type="org.hswebframework.web.dao.crud.TestEntity">
        <!--这里需要声明id-->
        <id property="id" column="id" javaType="Long" jdbcType="INTEGER"/>
        <!--如果没有使用jpa注解,需要在这里添加配置,因为动态生成sql的时候是根据resultMap中的字段进行配置的-->
    </resultMap>

   <!--另外一个resultMap,用于演示表关联-->
    <resultMap id="TestNestResultMap" type="org.hswebframework.web.dao.crud.TestEntity">
        <id property="id" column="id" javaType="Long" jdbcType="INTEGER"/>
        <!--jpa目前不支持表关联的解析,所以要在这里定义另外一个关联实体的全部信息,其中,column=关联表别名.列名-->
        <result property="nest.name" column="nest_table.name" javaType="String" jdbcType="VARCHAR"/>
    </resultMap>

    <!--用于动态生成sql所需的配置-->
    <sql id="config">
        <!--声明2个在生成动态sql时需要用到的变量-->
        <!--注意value里的单引号,因为value属性为一个ognl表达式-->
        <bind name="resultMapId" value="'TestResultMap'"/> 
        <bind name="tableName" value="'h_test'"/>
    </sql>

    <!--注意:keyColumn，keyProperty，useGeneratedKeys，只有在数据库生成id的时候才需要配置-->
    <insert id="insert" keyColumn="id" keyProperty="id" useGeneratedKeys="true" parameterType="org.hswebframework.web.dao.crud.TestEntity">
        <include refid="config"/>
        <include refid="BasicMapper.buildInsertSql"/>
    </insert>

    <update id="update" parameterType="org.hswebframework.web.commons.entity.Entity">
        <include refid="config"/>
        <include refid="BasicMapper.buildUpdateSql"/>
    </update>

    <select id="query" parameterType="org.hswebframework.web.commons.entity.Entity" resultMap="TestResultMap">
        <include refid="config"/>
        <include refid="BasicMapper.buildSelectSql"/>
    </select>

    <select id="count" parameterType="org.hswebframework.web.commons.entity.Entity" resultType="int">
        <include refid="config"/>
        <include refid="BasicMapper.buildTotalSql"/>
    </select>
    
    <delete id="delete" parameterType="org.hswebframework.web.commons.entity.Entity">
        <include refid="config"/>
        <include refid="BasicMapper.buildDeleteSql"/>
    </delete>
    
     <!--表关联的查询-->
    <select id="queryNest" parameterType="org.hswebframework.web.commons.entity.Entity" resultMap="TestNestResultMap">
        <bind name="tableName" value="'h_test'"/>
        <bind name="resultMapId" value="'TestNestResultMap'"/>
        select
        <include refid="BasicMapper.buildSelectField"/>
        from h_test <!--注意h_nest_table的别名:nest_table-->
        left join h_nest_table nest_table on nest_table.id=h_test.id
        <where>
            <include refid="BasicMapper.buildWhere"/>
        </where>
        <include refid="BasicMapper.buildSortField"/>
    </select>

    <select id="countNest" parameterType="org.hswebframework.web.commons.entity.Entity" resultType="int">
        <include refid="config"/>
        select
        count(1)
        from h_test
        left join h_nest_table nest_table on nest_table.id=h_test.id
        <where>
            <include refid="BasicMapper.buildWhere"/>
        </where>
    </select>

</mapper>

```

⚠️注意：query(count),update,delete方法的参数目前仅实现了:

`org.hswebframework.web.commons.entity.param.QueryParamEntity`

`org.hswebframework.web.commons.entity.param.UpdateParamEntity`

`org.hswebframework.web.commons.entity.param.DeleteParamEntity`

因此在实际调用都时候,目前只能接收对应以上参数.
但是在接口中仍然使用`org.hswebframework.web.commons.entity.Entity`作为参数,用于预留给以后提供更多都参数实现.

# 动态条件
此模块使用hsweb-easyorm项目来进行动态SQL条件的生成,主要是通过:在上述的`**ParamEntity`类中的属性 `List<Term> terms;`
进行处理,`Term`为一个SQL条件,例如一个简单的嵌套条件:
```text
    [{
      column:"name",
      termType:"eq", #SQL条件类型 =
      value:"张三",
      terms:[
        {
          column:"address",
          termType:"like", //SQL条件类型 like
          value:"北京%"
         },
         {
           column:"address",
           type:"or" //和前面的条件成or关系,如果不指定,默认为and
           termType:"like", //SQL条件类型 like
           value:"上海%"
          }
        ]
      }]
```
对应的sql条件为: where name = ? and (address like ? or address lke ?)

条件构造方式看上去过于复杂? 可以使用DSL方式的构建工具类:`org.hswebframework.ezorm.core.dsl.Query`来进行构建.
在[hsweb-commons-service-api](../../hsweb-commons-service/hsweb-commons-service-api)模块也会提供便捷的条件创建方式.

如果参数来自客户端请求,可封装一个通用的js进行构建, 你可以在前端放心的构建参数,所有的条件都使用参数化预编译的方式拼接SQL,不存在SQL注入问题.

## 默认支持的动态条件列表
| termType       | 对应SQL          |   说明 |
| ------------- |:-------------:| ----|
|eq|=?| 等于|
|not |!=?| 不等于|
|gt|>?| 大于|
|gte|>= ?| 大于等于|
|lt|< ?| 小于|
|lte| <= ?|  小于等于|
|like|like ?| 模糊匹配,如果需要统配符,请自行拼接value,如: value+"%" |
|nlike|not like ?| 同like |
|in|in(?,?)| value 可使用半角逗号(,)分隔,或者数组或者`Collection`接口的实现 |
|nin|not in(?,?)| value 可使用半角逗号(,)分隔,或者数组或者`Collection`接口的实现 |
|isnull| is null | value为任意不为空的值即可 |
|notnull| not null | value为任意不为空的值即可 |
|empty| ='' | value为任意不为空的值即可 |
|nempty| !='' | value为任意不为空的值即可 |
|bwt|between ? and ? | value 可使用半角逗号(,)分隔,或者使用数组或者Collection接口的实现|
|nbwt|not between ? and ? | value 可使用半角逗号(,)分隔,或者使用数组或者Collection接口的实现|

## 拓展动态条件
在某些需要自定义查询条件的场景,比如关联条件,可通过实现`SqlTermCustomer`接口并注入到spring来进行自定义SQL条件的拼接,
例如:
```java
//AbstractSqlTermCustomer提供了一些便利的方法
@org.springframework.stereotype.Component
public class MyTerm extends AbstractSqlTermCustomer{

    @Override
    public String getTermType() {
        //对应Term参数中的属性termType
        return "my-term";
    }

    @Override
    public Dialect[] forDialect() {
        //对特定对数据库类型生效,返回null时对全部支持对数据库类型生效
        return null;
    }


    @Override
    public SqlAppender accept(String wherePrefix, Term term, RDBColumnMetaData column, String tableAlias) {
        //当传入了my-term条件对时候,会调用此方法进行拼接
        //对传入对参数进行转换,此步骤为必须的
       ChangedTermValue termValue =createChangedValue(term);
        
       //转换参数,将参数转为集合,以支持in查询. 
       List<Object> idList = BoostTermTypeMapper.convertList(column, termValue.getOld());
        
       SqlAppender appender=  new SqlAppender();
      
       appender.add(createColumnName(column,tableAlias),"in (select id from my_table where t_id");
        
        //根据参数的数量,构造对应的=或者in条件
       Object newValue= appendCondition(idList,wherePrefix,appender);
        
       appender.add(")");
       //设置新的值到条件中
       termValue.setValue(newValue);
        
        return appender;
    }
}
```

使用: 在查询的时候,将`Term`的`termType`属性值设置为`my-term`即可.
