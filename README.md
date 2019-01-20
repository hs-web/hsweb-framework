## hsweb 后台管理基础框架
[![Maven Central](https://img.shields.io/maven-central/v/org.hswebframework.web/hsweb-framework.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.hswebframework)
[![Codecov](https://codecov.io/gh/hs-web/hsweb-framework/branch/master/graph/badge.svg)](https://codecov.io/gh/hs-web/hsweb-framework/branch/master)
[![Build Status](https://travis-ci.org/hs-web/hsweb-framework.svg?branch=master)](https://travis-ci.org/hs-web/hsweb-framework)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

  [贡献代码](CONTRIBUTING.md)  [快速开始](quick-start)

## 应用场景
1. 完全开源的后台管理系统.
2. 细粒度(按钮,行,列)权限控制的后台管理系统.
3. 模块化的后台管理系统.
4. 功能可拓展的后台管理系统.
5. 集成各种常用功能的后台管理系统.
6. 前后分离的后台管理系统.

注意:
项目主要基于`spring-boot`,`mybatis`. 在使用`hsweb`之前,你应该对`spring-boot`有一定的了解.

项目模块太多?不要被吓到.我们不推荐将本项目直接`clone`后修改,运行.而是使用maven依赖的方式使用`hsweb`. 
选择自己需要的模块进行依赖,正式版发布后,所有模块都将发布到maven中央仓库.
你可以参照[demo](https://github.com/hs-web/hsweb3-demo)进行使用.

## 文档
各个模块的使用方式查看对应模块下的 `README.md`,在使用之前,
你可以先粗略浏览一下各个模块,对每个模块的作用有大致的了解.

## 核心技术选型

1. Java 8
2. Maven3
3. Spring Boot 1.5.x
4. Mybatis 
5. Hsweb Easy Orm (使用`hsweb-easy-orm`拓展`Myabtis`实现动态条件)

## 模块简介

| 模块       | 说明          |   进度 |
| ------------- |:-------------:| ----|
|[hsweb-authorization](hsweb-authorization)|权限控制| 100%|
|[hsweb-commons](hsweb-commons) |基础通用功能| 100%|
|[hsweb-concurrent](hsweb-concurrent)|并发包,缓存,锁,计数器等| 80%|
|[hsweb-core](hsweb-core)|框架核心,基础工具类| 100%|
|[hsweb-datasource](hsweb-datasource)|数据源| 100%|
|[hsweb-logging](hsweb-logging)| 日志|  100%|
|[hsweb-starter](hsweb-starter)|模块启动器| 100%|
|[hsweb-system](hsweb-system)|**系统常用功能**| 80%|
|[hsweb-thirdparty](hsweb-thirdparty)| 第三方插件 | 100% |

## 核心特性
1. DSL风格,可拓展的通用curd,支持前端直接传参数,无需担心任何sql注入.
```java
  //where name = #{name} limit 0,20
  createQuery().where("name",name).list(0,20);
  
  //update s_user set name = #{user.name} where id = #{user.id}
  createUpdate().set(user::getName).where(user::getId).exec();
```

2. 灵活的权限控制
```java

@PostMapping("/account")
@Authorize(permission="account-manager",action="add")
public ResponseMessage<Sring> addAccount(@RequestBody Account account){
  return ok(accountService.addAccount(account));
}

@GettMapping("/account")
@Authorize(permission="account-manager",action="query",dataAccess=@RequiresDataAccess)//开启数据权限控制
public ResponseMessage<PageResult<Account>> addAccount(QueryParamEntity query){

  //用户设置了数据权限后,query的参数属性将被修改
  
  return ok(accountService.selectPager(query));
}


```

3. 灵活的模块版本维护脚本

`resources/hsweb-starter.js`

```js
//组件信息
var info = {
    groupId: "com.company",
    artifactId: "module-name",
    version: "1.0.2",
    website: "company.com",
    author: "作者",
    comment: "模块名称"
};

//版本更新信息
var versions = [
    {
        version: "1.0.2", //当info.version大于等于此版本号时,执行upgrade
        upgrade: function (context) {
            var database = context.database;
            //增加冻结金额字段
            database.createOrAlter("acc_account")
                .addColumn().name("freeze_balance").jdbcType(JDBCType.BIGINT).comment("冻结金额").commit()
                .comment("资金账户")
                .commit();
        }
    }

];
var JDBCType = java.sql.JDBCType;

//首次引入依赖,将执行安装操作
function install(context) {
    var database = context.database;
    database.createOrAlter("acc_account")
        .addColumn().name("id").varchar(32).notNull().primaryKey().comment("ID").commit()
        .addColumn().name("account_no").varchar(32).notNull().comment("资金账户号").commit()
        //更多字段
        //索引
        .index().name("idx_acc_account_no")
        .column("account_no").commit()//account_no索引
        .comment("资金账户").commit();
}

//设置依赖,固定代码,无需修改
dependency.setup(info)
    .onInstall(install) //安装时执行
    .onUpgrade(function (context) { //更新时执行
        var upgrader = context.upgrader;
        upgrader.filter(versions) //过滤版本信息
            .upgrade(function (newVer) { //执行更新
                newVer.upgrade(context);
            });
    })
    .onUninstall(function (context) { //卸载时执行

    });
```
