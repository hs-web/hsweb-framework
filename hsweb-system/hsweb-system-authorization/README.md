## 权限功能模块

1. 提供用户,角色管理等基础功能
2. 提供统一的多维度,可拓展的权限分配

        权限设置不再像以往那样和角色,用户直接关联.在此模块里,权限设置是通用的.
        你可以为用户,角色,自己定义的维度比如:机构,部门,岗位等维度进行权限分配.
        而且不仅仅支持基本等RBAC权限控制,还可以自定义控制到数据行和列.
        
3. 提供系统菜单管理
        
## 使用
引入依赖到`pom.xml`
```xml
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-system-authorization-starter</artifactId>
    <version>${hsweb.framework.version}</version>
</dependency>
```

## 结构
 
![uml](./uml.png "uml.png")

## 统一认证的用户状态

`DefaultReactiveAuthenticationInitializeService#initUserAuthorization` 读取用户，
`doInit` 复用该用户的状态进行准入：只有状态为 `1` 的启用用户才初始化权限并发布
`AuthorizationInitializeEvent`，停用、不存在或状态为空的用户返回空结果。
该规则也作用于直接调用 `doInit` 的入口，不依赖事件监听器补充校验。

`DefaultReactiveAuthenticationManager#getByUserId` 仅缓存并委派 initializer，不额外查询用户。
默认 initializer 的启用用户冷加载读取一次用户；缓存命中不读取用户。
无缓存时使用同一 initializer。用户读取异常沿用原链路错误处理，无缓存时向调用方传播。
状态变更需要在事务提交后发布 `ClearUserAuthorizationCacheEvent`，随后访问按最新状态重新加载。

初始化接口、启用用户的权限构造和初始化事件语义保持不变。
该校验不取消已有在途请求。Guava、Caffeine 本地缓存会阻止失效时已撤销的加载者重新回填，
其后续完成或失败也不会清除新的加载者和缓存值。Redis 同节点异步写入及跨节点写入与失效
不提供同样的原子保证；`clear` 不提供全缓存事务。

状态校验仅作用于默认 initializer；自定义 initializer 可以按自身契约从其他来源创建认证，
认证管理器不会要求额外的持久用户。其他认证 Provider 仍按自身契约提供认证，
默认 initializer 返回空不会阻止其他 Provider 返回认证。
直接由独立 Token Provider 创建的机器身份不经过该用户加载入口。
