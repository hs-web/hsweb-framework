# hsweb4 基于spring-boot2,全响应式的后台管理框架

[![Codecov](https://codecov.io/gh/hs-web/hsweb-framework/branch/4.0.x/graph/badge.svg)](https://codecov.io/gh/hs-web/hsweb-framework/branch/master)
[![Build Status](https://api.travis-ci.com/hs-web/hsweb-framework.svg?branch=4.0.x)](https://travis-ci.com/hs-web/hsweb-framework)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

# 功能,特性

- [x] 基于[r2dbc](https://github.com/r2dbc) ,[easy-orm](https://github.com/hs-web/hsweb-easy-orm/tree/4.0.x) 的通用响应式CRUD
    - [x] H2,Mysql,SqlServer,PostgreSQL
- [x] 响应式r2dbc事务控制
- [x] 响应式权限控制,以及权限信息获取
    - [x] RBAC权限控制
    - [x] 数据权限控制
    - [ ] 双因子验证
- [x] 多维度权限管理功能
- [x] 响应式缓存
- [ ] 非响应式支持(mvc,jdbc)
- [ ] 内置业务功能
    - [x] 权限管理
        - [x] 用户管理
        - [x] 权限设置
        - [x] 权限分配
    - [ ] 文件上传
        - [x] 静态文件上传
        - [ ] 文件秒传
    - [x] 数据字典

# 示例

https://github.com/zhou-hao/hsweb4-examples

## 应用场景

1. 完全开源的后台管理系统.
2. 模块化的后台管理系统.
3. 功能可拓展的后台管理系统.
4. 集成各种常用功能的后台管理系统.
5. 前后分离的后台管理系统.

注意:
项目主要基于`spring-boot`,`spring-webflux`. 在使用`hsweb`之前,你应该对 [project-reactor](https://projectreactor.io/) ,
[spring-boot](https://github.com/spring-projects/spring-boot) 有一定的了解.

项目模块太多?不要被吓到.我们不推荐将本项目直接`clone`后修改,运行.而是使用maven依赖的方式使用`hsweb`. 选择自己需要的模块进行依赖,正式版发布后,所有模块都将发布到maven中央仓库.

## 文档

各个模块的使用方式查看对应模块下的 `README.md`,在使用之前, 你可以先粗略浏览一下各个模块,对每个模块的作用有大致的了解.

## 核心技术选型

1. Java 8
2. Maven3
3. Spring Boot 2.x
4. Project Reactor 响应式编程框架
5. hsweb easy orm 对r2dbc的orm封装

## 模块简介

| 模块       |     说明     |  
| ------------- |:----------:| 
|[hsweb-authorization](hsweb-authorization)|    权限控制    |
|[hsweb-commons](hsweb-commons) |   基础通用功能   | 
|[hsweb-concurrent](hsweb-concurrent)|  并发包,缓存,等  | 
|[hsweb-core](hsweb-core)| 框架核心,基础工具类 | 
|[hsweb-datasource](hsweb-datasource)|    数据源     | 
|[hsweb-logging](hsweb-logging)|     日志     |  
|[hsweb-starter](hsweb-starter)|   模块启动器    | 
|[hsweb-system](hsweb-system)| **系统常用功能** |

## 核心特性

1. 响应式,首个基于spring-webflux,r2dbc,从头到位的响应式.
2. DSL风格,可拓展的通用curd,支持前端直接传参数,无需担心任何sql注入.

```java
  //where name = #{name}
  createQuery()
          .where("name",name)
          .fetch();

          //update s_user set name = #{user.name} where id = #{user.id}
          createUpdate()
          .set(user::getName)
          .where(user::getId)
          .execute();

```

3. 类JPA增删改

```java

@Table(name = "s_entity")
public class MyEntity {
    
    @Id
    private String id;
    
    @Column
    private String name;

    @Column
    private Long createTime;
}

```

直接注入即可实现增删改查

```java

@Autowire
private ReactiveRepository<MyEntity, String> repository;

```

2. 灵活的权限控制

```java

@PostMapping("/account")
@SaveAction
public Mono<String> addAccount(@RequestBody Mono<Account> account){
     return accountService.doSave(account);
}

```

## License

[Apache 2.0](https://github.com/spring-projects/spring-boot/blob/main/LICENSE.txt)