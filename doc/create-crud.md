# 创建通用增删改查功能

## 1. 实体
目前hsweb只有一种实体:PO。统一继承 `GenericPo`

新建实体`org.hsweb.demo.bean.test.MyTest`如下:
```java
    public class MyTest extends GenericPo{
        private String name;
        
        private int age;
        
        //由于查询使用动态参数,使用此方式定义属性名。方便统一维护
        public interface Property extends GenericPo.Property{
            String name = "name";
            String age  = "age";
        }
    }
```

建立数据库表(hsweb暂未使用jpa等方式自动建表):

方式1: 编辑`resources/scripts/initialize.groovy` (此脚本在项目首次运行时执行) 并加入
```groovy
database.createOrAlter("s_test")
        .addColumn().name("u_id").alias("id").comment("ID").jdbcType(JDBCType.VARCHAR).length(32).primaryKey().commit()
        .addColumn().name("age").alias("age").comment("年龄").jdbcType(JDBCType.DECIMAL).length(16,0).commit()
        .addColumn().name("name").alias("name").comment("姓名").jdbcType(JDBCType.VARCHAR).length(128).commit()
        .comment("测试").commit();
```

方式2: 如果系统已经初始化,则需要手动建立表结构,或者使用更新版本的方式进行初始化:
假设当前版本为 1.0.0, 升级为 1.0.1,则新建文件 ``resources/scripts/upgrade/1.0.1.groovy`` 并加入方式1中的脚本内容.
在启动后,更新版本时会自动执行此脚本.

## 2. dao 接口 

定义接口 ``org.hsweb.demo.dao.test.MyTestMapper``

(增删改查 继承GenericMapper即可)
```java
    public interface MyTestMapper extends GenericMapper<MyTest, String> {
    }
```
## 3.mybatis dao实现
mybatis 采用配置文件(xml)的方式

新建xml配置`resources/org/hsweb/demo/mappers/test/MyTestMapper`如下:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
    <!DOCTYPE mapper
            PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
            "http://www.mybatis.org/dtd/mybatis-3-mapper.dtd">
    <mapper namespace="org.hsweb.demo.dao.test.MyTestMapper">
        <resultMap id="TestResultMap" type="org.hsweb.demo.bean.test.MyTest">
            <id property="id" column="u_id" javaType="string" jdbcType="VARCHAR"/>
            <result property="name" column="name" javaType="String" jdbcType="VARCHAR"/>
            <result property="age" column="age" javaType="int" jdbcType="INTEGER"/>
        </resultMap>
    
        <!--用于动态生成sql所需的配置-->
        <sql id="config">
            <!--动态sql使用resultMapId对应的配置，来生成sql-->
            <bind name="resultMapId" value="'TestResultMap'"/>
            <bind name="tableName" value="'s_test'"/>
        </sql>
        <insert id="insert" parameterType="org.hsweb.web.bean.common.InsertParam">
            <include refid="config"/>
            <include refid="BasicMapper.buildInsertSql"/>
        </insert>
    
        <delete id="delete" parameterType="org.hsweb.web.bean.common.DeleteParam">
            <include refid="config"/>
            <include refid="BasicMapper.buildDeleteSql"/>
        </delete>
    
        <update id="update" parameterType="org.hsweb.web.bean.common.UpdateParam">
            <include refid="config"/>
            <include refid="BasicMapper.buildUpdateSql"/>
        </update>
    
        <select id="selectByPk" parameterType="string" resultMap="TestResultMap">
            select * from s_test where u_id=#{id}
        </select>
    
        <select id="select" parameterType="org.hsweb.web.bean.common.QueryParam" resultMap="TestResultMap">
            <include refid="config"/>
            <include refid="BasicMapper.buildSelectSql"/>
        </select>
    
        <select id="total" parameterType="org.hsweb.web.bean.common.QueryParam" resultType="int">
            <include refid="config"/>
            <include refid="BasicMapper.buildTotalSql"/>
        </select>
    </mapper>
```

## 4. service 接口

定义service接口 ``org.hsweb.demo.service.test.MyTestService``

(增删改查 GenericService)
```java
public interface MyTestService extends GenericService<MyTest, String> {
}
```

## 5. service 实现

定义service实现类 ``org.hsweb.demo.service.test.impl.SimpleMyTestService``

继承 AbstractServiceImpl
```java
@Service("testService")
public class SimpleMyTestService extends AbstractServiceImpl<MyTest, String> implements MyTestService {
    @Autowired
    private MyTestMapper myTestMapper;
    
    //AbstractServiceImpl 使用GenericMapper的实现类进行CRUD操作
    @Override
    protected MyTestMapper getMapper() {
        return myTestMapper;
    }
}
```

## 6. controller

定义Controller ``org.hsweb.demo.controller.test.MyTestController``

```java
@RestController
@RequestMapping("/myTest")
@Authorize(module = "myTest") //权限验证
@AccessLogger("测试模块")   //访问日志描述
public class MyTestController extends GenericController<MyTest, String> {

    @Autowired
    MyTestService myTestService;
    
    @Override
    protected MyTestService getService() {
        return myTestService;
    }
}
```

## 7. 添加权限

1、启动并登录系统,进入系统管理-权限管理模块加入对应的权限,重新登录即可使用了。
2、或者参照初始化表的方式,以脚本的方式进行初始化,如:

```groovy
def module= [u_id: 'myTest', name: '测试', uri: 'admin/myTest/list.html', icon: '', parent_id: '-1', remark: '', status: 1, optional: '[{"id":"M","text":"菜单可见","checked":true},{"id":"import","text":"导入excel","checked":true},{"id":"export","text":"导出excel","checked":true},{"id":"R","text":"查询","checked":true},{"id":"C","text":"新增","checked":true},{"id":"U","text":"修改","checked":true},{"id":"D","text":"删除","checked":false}]', sort_index: 1];
database.getTable("s_modules").createInsert().value(module).exec();
```

## 8. 感觉太麻烦?

**使用在线代码生成器,一键生成全部代码!**