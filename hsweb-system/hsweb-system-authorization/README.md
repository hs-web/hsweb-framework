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

`UserEntity` 定义 `STATUS_DISABLED=0`、`STATUS_ENABLED=1`、`STATUS_LOCKED=2`，
及用户维度 options 的状态键 `OPTION_STATUS="status"`。

`DefaultReactiveAuthenticationInitializeService#initUserAuthorization` 读取用户，
`doInit` 复用该用户的状态进行准入：禁用或不存在的用户返回空结果；
锁定、状态为空及其他非禁用状态仍可初始化权限并发布 `AuthorizationInitializeEvent`。
该规则也作用于直接调用 `doInit` 的入口，不依赖事件监听器补充校验。
真实状态保存在 `SimpleUser` 的 options 中，空状态保留该键及空值，
不写入 `Authentication.attributes`，也不增加用户字段或查询。

内部读取权限与对外凭据访问分别准入。默认密码认证仅允许启用用户；
停用和锁定用户不能密码登录。既有用户事件的非启用状态 token 注销规则保持不变。

`DefaultReactiveAuthenticationManager#getByUserId` 仅缓存并委派 initializer，不额外查询用户。
默认 initializer 的非禁用用户冷加载读取一次用户；缓存命中不读取用户。
无缓存时使用同一 initializer。用户读取异常沿用原链路错误处理，无缓存时向调用方传播。
状态变更需要在事务提交后发布 `ClearUserAuthorizationCacheEvent`，随后访问按最新状态重新加载。

初始化接口、非禁用用户的权限构造和初始化事件语义保持不变。
该校验不取消已有在途请求。Guava、Caffeine 本地缓存会阻止失效时已撤销的加载者重新回填，
其后续完成或失败也不会清除新的加载者和缓存值。Redis 同节点异步写入及跨节点写入与失效
不提供同样的原子保证；`clear` 不提供全缓存事务。

状态校验仅作用于默认 initializer；自定义 initializer 可以按自身契约从其他来源创建认证，
认证管理器不会要求额外的持久用户。其他认证 Provider 仍按自身契约提供认证，
默认 initializer 返回空不会阻止其他 Provider 返回认证。
自定义来源为用户委托访问签发凭据时，需要在受信的用户 options 中提供真实状态；
应用认证入口据此判断是否允许访问，不从请求参数或 `Authentication.attributes` 推断状态。
直接由独立 Token Provider 创建的机器身份不经过该用户加载入口。

本轮与应用认证模块同源联合验证 9 类、143 项全部通过，使用当前 API、default 和 cache
源码产物。默认认证测试 6 项确认非禁用用户的实际权限、用户状态和初始化事件、
冷加载查询一次、热命中零次及密码准入；真实 PostgreSQL、Redis、H2 和 HTTP
验证锁定用户后台读取权限、旧授权码和委托 Token 拒绝、重新启用与删除回滚。
