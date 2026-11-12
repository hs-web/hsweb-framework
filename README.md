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

## 版本分支

`5.0.3` 维护 `5.0.3-SNAPSHOT`，`5.0.x` 继续开发后续补丁版本。根 POM 和所有子模块的父版本需同步更新，模块间依赖继续使用 `${project.version}`。

自动发布工作流明确监听 `5.0.x` 和 `5.0.3` 的 push，发布完整 Maven 工程，保证父 POM 与各模块使用同一版本。两条版本线的 Pull Request 都执行测试，按 `changes.sh` 选择变更模块并通过 `-am` 构建所需上游模块。切分维护分支时，先保留当前版本和发布触发，再升级开发分支版本；依赖库版本独立管理。

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

## 实体部分更新事件

`hsweb-commons-crud` 的 prepare / before / after 修改事件通过 `submittedValues` 提供只读的原始提交快照，保留显式 null 和虚拟属性；`before` / `after` 仍表示映射列视图。`Extendable.extensions()` 返回 null 时按空扩展处理，普通提交字段仍保留在快照中。该语义同样适用于未启用动态字段的实体更新，不改变原有事件启用规则或事务边界。

## 查询操作上下文

`QueryOperationContext.within(operation)` 为每次订阅创建独立的准备结果作用域。`QueryHelper.queryPager()` 在入口建立作用域，内部 count / rows 共享结果；嵌套的独立分页建立自己的作用域，完成后外层仍使用原有作用域。普通子查询继承所属操作的上下文；切换业务范围或授权上下文时，由拥有该切换的操作边界在新 Reactor Context 中使用 `within()` 包裹查询。缓存 key 不耦合租户或用户字段，缓存内容限于本次操作的不可变准备结果，不提供数据库事务快照。

## 本地响应式缓存失效

`EnableCacheReactiveCrudService.findById` 和 `GenericReactiveCacheSupportCrudService.getCacheAll` 在订阅时检查仓库的 `isResultCacheable()`。权限或上下文装配仓库返回 false 时绕过共享实体缓存，避免不同订阅者复用完整结果；普通仓库保留原有缓存命中与失效行为。结果转换器默认不可共享缓存，不依赖上下文且依赖数据更新由既有失效机制覆盖的转换器可显式声明 true，缓存服务无需识别转换器或业务类型。仓库装饰器负责声明其自身的上下文依赖，目标仓库可缓存不代表权限装饰器可以共享结果。`GenericReactiveCacheSupportCrudServiceTest` 用真实 H2 查询验证纯转换的 ID/全量缓存命中、写后失效，以及上下文转换绕过旧缓存。

`hsweb-concurrent/hsweb-concurrent-cache` 中的 Guava、Caffeine 缓存在
`evict`、`evictAll` 处理对应 key 时撤销已登记的加载者，`clear` 撤销处理时已有的加载者。
加载成功时通过现有加载映射在同一 key 的原子操作内校验身份并同步回填。
已撤销加载者的完成或错误不会回填缓存、移除之后的新加载者或清除其缓存值。

getter 在订阅时才登记加载者并开始缓存读取，避免仅装配 Publisher 就捕获旧值。
已经开始读取且与 `clear` 重叠的在途请求可以完成原值。`clear` 不是全缓存事务，
不保证撤销并发产生的新订阅，也不改变调用方默认加载来源的新鲜度。

Redis 的写入是异步远程操作，仍沿用原失效逻辑；上述本地保证不适用于
Redis 同节点异步写入或跨节点写入与失效。

## License

[Apache 2.0](https://github.com/spring-projects/spring-boot/blob/main/LICENSE.txt)


[![Stargazers over time](https://starchart.cc/hs-web/hsweb-framework.svg?variant=adaptive)](https://starchart.cc/hs-web/hsweb-framework)
