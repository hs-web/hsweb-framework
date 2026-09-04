# 用户令牌与响应式扩展认证

## 当前事实

响应式请求先由 `ReactiveUserTokenParser` 提取 `ParsedToken`，再由
`UserTokenWebFilter` 写入 Reactor Context。`UserTokenReactiveAuthenticationSupplier`
读取该上下文并通过 `UserTokenManager` 校验有状态用户令牌，最后加载
`Authentication`。

`ThirdPartReactiveAuthenticationManager` 的已发布职责是：有状态令牌经过
`UserTokenManager` 校验后，根据令牌记录中的 `userId` 和 `tokenType` 加载第三方权限。
它不负责验证请求携带的原始令牌。

新增的 `ReactiveTokenAuthenticationProvider` 用于按 `ParsedToken.type` 接管原始令牌的
响应式认证，例如结构化 API Token、个人令牌或其他无状态令牌。Provider 负责完整验证
令牌并返回最终 `Authentication`，不创建 hsweb 用户令牌记录。

## 本次目标

- 完成 `ReactiveTokenAuthenticationProvider` 公共 SPI 契约与自动注册。
- 按明确的 `tokenType` 唯一路由 Provider，禁止重复类型静默覆盖。
- Provider 命中后执行完整认证，失败或空结果不回退 `UserTokenManager`，避免非法结构化
  Token 触发旧令牌存储查询。
- 未命中 Provider 时完整保留现有 `UserTokenManager`、`AuthenticationUserToken` 和
  `ThirdPartReactiveAuthenticationManager#getByUserId` 行为。
- 让多个 `ReactiveUserTokenParser` 按 Spring Order 顺序、逐个解析，第一个产生结果的
  Parser 唯一拥有本次请求。

## 影响范围

- owning module：`hsweb-authorization/hsweb-authorization-api`
  - `ReactiveTokenAuthenticationProvider`
  - `UserTokenReactiveAuthenticationSupplier`
  - `ThirdPartReactiveAuthenticationManager`
  - Provider 路由、兼容性和安全回归测试
- transport module：`hsweb-authorization/hsweb-authorization-basic`
  - `ReactiveUserTokenParser` 顺序契约
  - `AuthorizingHandlerAutoConfiguration` 有序装配
  - `UserTokenWebFilter` 顺序解析及上下文保持
- 不涉及数据库、实体、SQL、事件、缓存、常驻任务或外部服务。

## 非目标

- 不在 hsweb 中实现 JetLinks API 应用、Principal、授权分组或具体结构化 Token 算法。
- 不规定 `ak_`、`tk_` 等业务前缀；这些由上层统一 Token 规则实现。
- 不修改 OAuth2 Token Endpoint、AccessTokenManager 或本次已提交的 OAuth2 SPI。
- 不扩展 Servlet 同步认证链；本次只完善响应式请求链。
- 不改变 `ReactiveAuthenticationHolder#get(String userId)` 的按用户 ID 权限查询语义；
  按不同 Principal 类型离线加载权限属于独立扩展边界。

## 设计约束

### Provider 路由

`ReactiveTokenAuthenticationProvider#getTokenType()` 返回稳定、非空、大小写敏感的唯一
路由键。初始化时空类型或重复类型立即失败，不能依赖 Bean 注册顺序覆盖。

`authenticate(ParsedToken)` 的调用契约：

- 只在 `ParsedToken.type` 精确匹配时调用，一次请求最多调用一个 Provider；
- Provider 必须完成签名、有效期、状态和授权上下文等该令牌类型要求的全部认证；
- 返回 `Authentication` 表示认证成功；`Mono.empty()` 表示认证失败；异常原样传播；
- Provider 已命中后，无论返回空或异常都不回退有状态令牌链；
- Provider 不得记录原始 Token 或把敏感凭证写入日志、异常、trace 属性。

### 兼容链路

```text
ParsedToken
  -> tokenType 命中 ReactiveTokenAuthenticationProvider
       -> provider.authenticate(parsedToken)
       -> 成功返回 Authentication；空或异常直接结束，不回退
  -> 未命中 Provider
       -> UserTokenManager.getByToken(rawToken)
       -> 过期、状态与 validate 校验
       -> touch
       -> AuthenticationUserToken 内嵌 Authentication
          或 ThirdPartReactiveAuthenticationManager#getByUserId(userId)
```

`ThirdPartReactiveAuthenticationManager` 保持原接口和原调用阶段，不增加原始 Token 认证
方法，以避免已发布实现因为默认空结果而失效。

### Parser 顺序

`ReactiveUserTokenParser` 按 Spring `Order` 从高到低顺序调用，使用 `concatMap` 保证前一
Parser 完成后才尝试下一 Parser。Parser 返回 `Mono.empty()` 表示不识别该请求；一旦返回
`ParsedToken`，后续 Parser 不再调用；异常终止解析，不回退其他 Parser。

统一结构化 Token Parser 应保持无阻塞、无数据库访问，只负责提取和低成本分类。未命中
保留前缀时返回空；命中保留前缀后，即使令牌非法也必须归属对应 Provider，不能回退普通
Bearer 或有状态 Token。`UserTokenWebFilter` 继续保留原有 `ReactiveLogger` 请求上下文。

## 实现落点

- `ReactiveTokenAuthenticationProvider` 定义原始令牌认证 SPI，并明确空结果、异常、敏感
  信息和无回退契约。
- `UserTokenReactiveAuthenticationSupplier` 独立维护 Provider 路由；空类型和重复类型在
  初始化时失败，未命中 Provider 时继续原有有状态令牌链。
- `ThirdPartReactiveAuthenticationManager` 保持已发布的 `getByUserId` 职责，并通过
  JavaDoc 与原始令牌认证 Provider 明确分工。
- `ReactiveUserTokenParser`、`AuthorizingHandlerAutoConfiguration` 和
  `UserTokenWebFilter` 共同保证 Spring Order、顺序解析、错误终止与请求日志上下文传播。
- `UserTokenReactiveAuthenticationSupplierTest` 覆盖 Provider 路由、安全无回退和旧链兼容；
  `UserTokenWebFilterTest` 覆盖首个命中、空结果继续、错误终止和日志上下文。

## 风险与验证方式

- 兼容风险：Provider 类型如果占用已有 `default`、`bearer` 等 Parser 类型，会明确接管该
  类型；文档和测试必须证明不存在隐式 fallback。
- 安全风险：Provider 命中后回退旧存储会放大碰撞查询，必须通过“空结果不调用
  UserTokenManager”测试锁定。
- 顺序风险：高优先级 Parser 执行 I/O 会阻塞后续 Parser，因此 SPI 契约要求 Parser 只做
  低成本识别，完整认证留在 Provider。
- 验证结果见下节；authorization-basic 测试装配显式声明 EasyORM 初始化所需的
  PostgreSQL R2DBC 测试依赖，不向生产依赖或认证逻辑扩散。
- TraceHolder：不新增手动 span；本次是现有 HTTP 请求内的认证路由，没有新增跨服务或
  后台异步边界，且不得将原始 Token 写入 trace。
- MBean：不适用；本次不新增缓存、队列、后台任务或其他常驻状态。
- i18n：不新增用户可见错误码；重复 Provider 类型属于启动配置错误。

## 验证结果

- 定向验证：
  `mvn -pl hsweb-authorization/hsweb-authorization-api,hsweb-authorization/hsweb-authorization-basic -am -Dtest=UserTokenReactiveAuthenticationSupplierTest,UserTokenWebFilterTest -Dsurefire.failIfNoSpecifiedTests=false test`
  执行成功，9 项测试通过，0 失败、0 错误、0 跳过。
- authorization-api 模块全量：17 项测试，0 失败、0 错误、1 项
  `RedisUserTokenManagerTest` 跳过。
- CI 同范围复验：
  `./mvnw test -q -pl hsweb-authorization/hsweb-authorization-api,hsweb-authorization/hsweb-authorization-basic`
  执行成功；authorization-api 共 17 项，0 失败、0 错误、1 项跳过；
  authorization-basic 共 7 项，0 失败、0 错误、0 跳过。
- `git diff --check` 通过。
