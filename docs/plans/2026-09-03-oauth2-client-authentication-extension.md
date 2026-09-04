# OAuth2 客户端认证与 client_credentials 扩展设计

> 状态：oauth2-extension-v1.5 已实现并验证（含 HTTP 集成测试与兼容检查）
>
> 契约版本：oauth2-extension-v1.5
>
> owning module：hsweb-authorization/hsweb-authorization-oauth2

## 1. 背景、目标与边界

当前 OAuth2 token endpoint 在 OAuth2AuthorizeController 查询 OAuth2Client 后直接调用
OAuth2Client.validateSecret。对于 client_credentials，它固定以 OAuth2Client.userId 解析
用户 Authentication，再由 AccessTokenManager 创建 Redis 会话型 Token。这适合“一个 client
对应一个用户且 secret 明文保存”的历史模型，不能安全支持多凭证、hash 校验、机器主体或
不同 Token 签发策略。

本优化只将两个变化轴变成 hsweb 原生扩展点：

    认证凭证 -> 已认证客户端上下文 -> client_credentials 签发处理器

- 客户端认证可替换，默认仍为 OAuth2ClientManager + validateSecret。
- client_credentials 按已认证客户端的稳定类型唯一选择处理器。
- HTTP、public API、Redis Token 与非 client_credentials grant 默认行为不变。

非目标：不重构 AccessTokenManager；不改 authorization_code、refresh_token 模型；不引入
Spring Security；不实现 SSO、OIDC、JWT Resource Server、mTLS、private_key_jwt 的业务
实现；不让 hsweb 依赖 JetLinks Principal、实体或存储。这些仅是外部模块以后可实现本 SPI
的认证方式，不是首期接口。

## 2. 当前事实与调用链

    GET/POST /oauth2/token
      -> OAuth2AuthorizeController#getClientIdAndClientSecret
         (Authorization: Basic 优先，否则 form/query client_id/client_secret)
      -> OAuth2ClientManager#getClient
      -> OAuth2Client#validateSecret
      -> GrantType#requestToken
      -> ClientCredentialGranter#requestToken（仅 client_credentials）
      -> DefaultClientCredentialGranter
      -> ReactiveAuthenticationHolder#get(client.userId)
      -> AccessTokenManager#createAccessToken
      -> OAuth2GrantedEvent

| 职责 | 当前类型 |
| --- | --- |
| HTTP、Basic/form 解析、grant 分派 | server.web.OAuth2AuthorizeController |
| 历史 client 查询 | server.OAuth2ClientManager |
| 历史 secret 校验 | server.OAuth2Client#validateSecret |
| client_credentials facade | credential.ClientCredentialGranter |
| 默认用户/Redis 签发 | credential.DefaultClientCredentialGranter |
| 自动装配 | server.OAuth2ServerAutoConfiguration |
| 授权成功事件 | event.OAuth2GrantedEvent |

## 3. 冻结的新增 API

新增类型属于 org.hswebframework.web.oauth2.server 或其 authentication / credential 子包。
实施可在首个提交固定具体包名，但不得改变以下职责和交互。

### 3.1 客户端认证 SPI

    public interface ReactiveOAuth2ClientAuthenticator {
        Mono<OAuth2ClientAuthentication> authenticate(
            OAuth2ClientAuthenticationRequest request);
    }

OAuth2ClientAuthenticationRequest 是 endpoint 的认证输入：

- clientId：从 Basic 或参数解析的标识；
- authenticationMethod：至少 client_secret_basic、client_secret_post；
- credentials：只用于当次校验的敏感值，建议 char[]；
- grantType：请求 grant_type；
- parameters：移除 client_secret 后的安全参数视图。

OAuth2ClientAuthentication 是成功输出：

- client：供 authorization_code、refresh_token、事件消费者使用的脱敏 OAuth2Client 兼容视图；
- clientType：稳定且非空的路由键，默认 default；
- attributes：仅认证后的非敏感扩展上下文。

attributes 可存 credential reference、授权版本、scope profile；不得存 raw secret、私钥、
完整 token 或可复用 HMAC key。SPI 不记录、打印或发布 credentials；构造请求时必须从
参数副本移除 client_secret。

默认 DefaultReactiveOAuth2ClientAuthenticator 必须与当前行为等价：

    OAuth2ClientManager#getClient(clientId)
      -> empty 时 ILLEGAL_CLIENT_ID
      -> OAuth2Client#validateSecret(credentials)
      -> OAuth2ClientAuthentication(client, default, empty attributes)

OAuth2ClientManager、OAuth2Client 的既有 public 方法不得删除或改签名；clientSecret /
validateSecret 继续可用，但不是扩展认证器的存储约束。

### 3.2 client_credentials 路由 SPI

ClientCredentialGranter 仍是 stable facade，保留：

    Mono<AccessToken> requestToken(ClientCredentialRequest request);

ClientCredentialRequest 新增可选 OAuth2ClientAuthentication 字段及：

    ClientCredentialRequest(OAuth2ClientAuthentication authentication,
                            Map<String, String> parameters);
    OAuth2ClientAuthentication getClientAuthentication();

旧构造器 ClientCredentialRequest(OAuth2Client, Map) 与 getClient() 不删、不改签名。旧构造器
内部创建 clientType=default 的等价认证上下文，历史调用方及自定义 facade 不必迁移。

    public interface ClientCredentialGrantHandler {
        String getClientType();
        Mono<AccessToken> requestToken(ClientCredentialRequest request);
    }

CompositeClientCredentialGranter implements ClientCredentialGranter 的规则：

- 构造期按 getClientType() 建唯一映射；空或重复 type 立即失败；
- 只按 request.getClientAuthentication().getClientType() 精确路由；
- 已识别 type 的 handler 失败时原样传播，绝不回退 default；
- 没有 handler 时复用现有 OAuth2Exception/ErrorType；首期不新增错误码。

DefaultClientCredentialGrantHandler 的 type 固定为 default，委托既有
DefaultClientCredentialGranter，复用用户 Authentication、AccessTokenManager 和
OAuth2GrantedEvent 语义，避免复制默认签发逻辑。

## 4. Controller 与 grant 交互

    解析 Basic/form/query
      -> OAuth2ClientAuthenticationRequest
      -> ReactiveOAuth2ClientAuthenticator#authenticate
      -> GrantType#requestToken(service, authentication, parameters)
      -> client_credentials: ClientCredentialRequest(authentication, parameters)
      -> authorization_code / refresh_token: authentication.getClient() 的脱敏兼容视图构造原请求

Controller 不再直接调用 OAuth2Client.validateSecret；调用留在默认 authenticator。
/oauth2/token、GET/POST 映射、Basic 优先级、form/query 参数、GrantType、成功 AccessToken
响应和 legacy OAuth2Exception 默认兼容。

保留三参构造器 (OAuth2GrantService, OAuth2ClientManager, OAuth2Properties)，内部使用默认
authenticator；新增带 ReactiveOAuth2ClientAuthenticator 的构造器供自动配置使用。

## 5. Bean 装配与覆盖规则

    无 ReactiveOAuth2ClientAuthenticator Bean
      -> DefaultReactiveOAuth2ClientAuthenticator(OAuth2ClientManager)

    存在任意 ClientCredentialGranter Bean
      -> 完全沿用该 facade；不注册 composite

    不存在 ClientCredentialGranter Bean
      -> CompositeClientCredentialGranter
         -> default handler（委托 DefaultClientCredentialGranter）
         -> 容器中全部 ClientCredentialGrantHandler

Controller 注入 authenticator；OAuth2GrantService 仍只依赖 facade。旧项目自定义一个
ClientCredentialGranter 的覆盖权不变，新项目只注册各 type handler。default 与外部
handler 同 type 必须在启动失败，不能按顺序覆盖。收集 handler 时不得把 composite 自身
纳入，避免循环依赖。

## 6. 错误、事件与敏感信息

- 默认 authenticator：client 不存在仍为 ILLEGAL_CLIENT_ID，secret 错误仍为
  ILLEGAL_CLIENT_SECRET。
- 自定义 authenticator 和无 handler 复用既有 OAuth2Exception/ErrorType；首期不增
  对外错误码。
- Controller、SPI、日志、异常、OAuth2GrantedEvent.parameters 都不得含 raw secret；
  grant request 参数副本必须已经排除 client_secret。
- handler 事件只能携带认证后的安全上下文；默认 handler 保持既有事件字段。外部机器
  client 是否发布事件、如何关联主体由 owning 模块决定，不得伪造用户 Authentication。
- client 识别和 secret 校验是一项认证决策：类型已识别后校验失败，不得转交另一
  authenticator 或 default handler，以避免 credential confusion。

## 7. JetLinks 接入边界

    ApiApplicationOAuth2ClientAuthenticator
      -> 查询 API 应用 Credential、校验 hash/状态
      -> 返回 clientType=api-application 的认证上下文

    ApiApplicationClientCredentialGrantHandler
      -> 为已认证 API 应用签发短期无状态 Token
      -> refresh token 为空，不创建用户或 Redis 会话

hsweb 仅识别 OAuth2Client 兼容视图、clientType 和安全 attributes；不依赖 JetLinks
application、credential、principal、permission 或签名 Token。权限解析、Token 验签与资源
授权不属于本模块。

## 8. 迁移与发布顺序

1. hsweb 增加 SPI、默认 authenticator、handler/composite 与兼容测试。
2. 演进 Controller/自动配置；无扩展 Bean 时行为必须等价旧版本。
3. 发布包含新 API 的 hsweb 版本；旧 manager、client、granter 继续工作。
4. JetLinks 升级依赖后注册 authenticator/handler，灰度 API 应用；历史 client 走 default。
5. 回归确认错误、事件、Redis Token、authorization_code/refresh_token 均不变。

## 9. v1.1 安全与兼容冻结修订

本节替代前述内容中与以下规则冲突的表述；其余 v1 契约继续有效。

### 9.1 脱敏 OAuth2Client 兼容视图

OAuth2ClientAuthentication 不得保存或向任何 grant、handler、event 暴露 manager-owned 的
OAuth2Client 实例。getClient() 必须返回认证成功后创建的脱敏快照/兼容视图：

- clientSecret 必须为 null；
- clientId、name、description、redirectUrl、userId 及其他非敏感既有字段保持值兼容；
- 认证器私有的 credential reference、授权版本、scope profile 等扩展状态只放入
  attributes；
- attributes 仍不得携带 raw secret、私钥、完整 Token 或签名 key。

默认 authenticator 允许使用 manager 返回的 OAuth2Client 完成 validateSecret，但完成后必须
立即创建脱敏视图再构造 OAuth2ClientAuthentication。旧 OAuth2Client public API 不改；
本规则限定新增认证上下文和其下游消费者，防止 handler 通过 getClient().getClientSecret()
取得密钥。

### 9.2 granter 参数的有意安全收紧

token endpoint 向 ClientCredentialGranter 和 ClientCredentialGrantHandler 传递的
ClientCredentialRequest.parameters 必须移除 client_secret。这是有意的安全收紧：

- ClientCredentialGranter、ClientCredentialRequest 的方法签名和旧构造器保持兼容；
- 旧自定义 ClientCredentialGranter 若读取 request.parameters.client_secret，升级后必须
  迁移为 ReactiveOAuth2ClientAuthenticator；
- authenticator 校验凭证后只能向 attributes 放 credential reference 等非敏感标识，不能
  重新传递 raw secret；
- 该变更不得被兼容分支绕回；发布说明与迁移指南必须把它列为“API 签名兼容、扩展行为安全
  收紧”。

| 兼容面 | v1.1 结论 | 升级动作 |
| --- | --- | --- |
| ClientCredentialGranter 方法签名 | 保持 | 无 |
| ClientCredentialRequest 旧构造器/getClient | 保持 | 无 |
| request.parameters.client_secret | 有意移除 | 迁移到 authenticator，使用 attributes credential reference |
| OAuth2ClientAuthentication.getClient | 返回脱敏兼容视图 | 不得将 clientSecret 作为下游输入 |

### 9.3 Basic 认证输入规则

Basic 的合法请求优先级不变：存在合法 Authorization: Basic 时，仍优先于 form/query
client_id、client_secret。解析规则收紧为：

- 仅以第一个冒号分隔 client id 与 secret，后续冒号属于 secret；
- 缺少冒号时，client id 为解码结果，secret 为空；
- 冒号前为空时 client id 为空，冒号后为空时 secret 为空；
- 空 client id 或空 secret 必须进入既有 OAuth2 错误语义，不得使用历史 id==secret 回退，
  也不得产生 ArrayIndexOutOfBoundsException；
- 无法解析的 Basic 值同样必须规范化为既有 OAuth2 认证错误，不暴露解析异常或响应式链外
  的 500。

该规则只收紧 malformed input；合法 Basic、GET/POST 和 Basic 优先级均保持不变。

### 9.4 已知装配边界

首期 OAuth2AuthorizeController 继续由 OAuth2ClientManager 条件装配，即使应用已提供
自定义 ReactiveOAuth2ClientAuthenticator 也仍需要 OAuth2ClientManager。原因是
/oauth2/authorize 与 token endpoint 共用同一个 Controller，前者仍直接依赖 manager。
首期不拆分 Controller 或端点条件；独立 authenticator-only server 不在本版本支持范围，必须
在后续独立设计中处理。

## 10. v1.1 测试矩阵补充

本次实现已覆盖以下验收：

| 场景 | 验收预期 |
| --- | --- |
| default authenticator 成功 | manager-owned 原 client 用于校验；下游 getClient 的 clientSecret 为 null |
| 自定义 handler | client 强制脱敏；attributes 按 SPI 契约仅允许非敏感引用，并验证 client_secret key 被剔除 |
| legacy custom granter 迁移 | 参数不含 client_secret；文档化迁移到 authenticator/credential reference |
| Basic 合法 clientId:secret:with:colon | 仅第一个冒号分隔，完整 secret 参与认证；Basic 优先级保持 |
| Basic 缺少分隔符、空 id、空 secret、无效 Base64 或非法 UTF-8 | 在 reactive 链内返回既有 OAuth2 认证错误，不调用自定义 authenticator，无同步 AIOOBE 或 500 |
| authorization_code | 使用脱敏 client 兼容视图，原 grant 结果与错误保持 |
| refresh_token | 使用脱敏 client 兼容视图，原 grant 结果与错误保持 |
| client_credentials 路由 | 精确按 clientType 路由，重复/空 type 失败，未知 type 或 handler 失败均不 fallback |
| credentials 清理 | complete、error、cancel、同步抛异常均清理 request credentials |

## 11. 实施落点与验证

### 实施落点

认证 SPI（4 类）：

- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/authentication/ReactiveOAuth2ClientAuthenticator.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/authentication/OAuth2ClientAuthenticationRequest.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/authentication/OAuth2ClientAuthentication.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/authentication/DefaultReactiveOAuth2ClientAuthenticator.java`

client_credentials 扩展：

- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/credential/ClientCredentialRequest.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/credential/ClientCredentialGrantHandler.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/credential/DefaultClientCredentialGrantHandler.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/credential/CompositeClientCredentialGranter.java`

端点与装配：

- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/web/OAuth2AuthorizeController.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/OAuth2ServerAutoConfiguration.java`

对应测试（6 组）：

- `hsweb-authorization/hsweb-authorization-oauth2/src/test/java/org/hswebframework/web/oauth2/server/authentication/DefaultReactiveOAuth2ClientAuthenticatorTest.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/test/java/org/hswebframework/web/oauth2/server/credential/ClientCredentialRequestCompatibilityTest.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/test/java/org/hswebframework/web/oauth2/server/credential/CompositeClientCredentialGranterTest.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/test/java/org/hswebframework/web/oauth2/server/credential/DefaultClientCredentialGrantHandlerTest.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/test/java/org/hswebframework/web/oauth2/server/OAuth2ServerAutoConfigurationTest.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/test/java/org/hswebframework/web/oauth2/server/web/OAuth2AuthorizeControllerTest.java`

### 验证结果与剩余风险

- 定向验证：29 / 0 / 0 / 0。命令：
  `mvn -pl hsweb-authorization/hsweb-authorization-oauth2 -am -Dsurefire.failIfNoSpecifiedTests=false -Dtest=OAuth2ServerAutoConfigurationTest,DefaultReactiveOAuth2ClientAuthenticatorTest,ClientCredentialRequestCompatibilityTest,CompositeClientCredentialGranterTest,DefaultClientCredentialGrantHandlerTest,OAuth2AuthorizeControllerTest test`
- owning module 验证：43 / 0 / 0 / 2。命令：
  `mvn -pl hsweb-authorization/hsweb-authorization-oauth2 test`
- 跳过项：RedisAccessTokenManagerTest、DefaultAuthorizationCodeGranterTest。
- DefaultClientCredentialGrantHandler 的透明委托已测试。实际 DefaultClientCredentialGranter
  的 OAuth2GrantedEvent 与 removeToken rollback 没有新增端到端测试；该源码逻辑本次未改，
  仍是剩余验证风险。
- 独立安全/兼容审查结论：无 P0-P2。malformed Basic、严格 UTF-8、脱敏 client 与无 fallback
  均已闭环。

实现前工作区中的用户原有修改均未被回退：

- `hsweb-authorization-basic/src/main/java/org/hswebframework/web/authorization/basic/web/UserTokenWebFilter.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/credential/DefaultClientCredentialGranter.java`
- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/web/OAuth2AuthorizeController.java`

其中 `UserTokenWebFilter` 继续作为未提交改动保留在工作区，未进入本 PR；
`DefaultClientCredentialGranter` 通过 `ReactiveAuthenticationHolder` 获取用户
Authentication 的既有行为已在本功能提交中保持。

本设计已按 oauth2-extension-v1.1 落地：客户端认证与 client_credentials 类型路由成为
向后兼容的扩展点；token endpoint 向 granter/handler 移除 client_secret 是有意的安全收紧，
下游 OAuth2Client 均为脱敏兼容视图。

## 12. HTTP 集成测试与清理

POM 新增最小 `spring-test` test scope 依赖，未引入
`spring-boot-starter-test`。新增
`OAuth2TokenEndpointAutoConfigurationIntegrationTest`，以
`AnnotationConfigReactiveWebApplicationContext + @EnableWebFlux +
WebTestClient.bindToApplicationContext` 调用 `/oauth2/token`；测试经过真实
DispatcherHandler、form 解码、参数绑定、response codec 和 OAuth2 auto-configuration，不启
真实端口或 Redis。

HTTP 集成覆盖：

1. GET Basic 的 default authenticator、legacy `ClientCredentialGranter` facade backoff、
   Basic 优先级和冒号 secret；
2. POST form 的 custom API authenticator -> composite handler，验证 secret 仅对
   authenticator 可见、后续 parameters/client 脱敏及 credentials 清理；
3. secret 认证失败不进入 handler；
4. unknown clientType 不回退 default handler；
5. malformed Basic 不进入 custom authenticator。

已完成清理：

- Controller 删除 unused imports，内部 `authenticateClient` helper 收回 private；
- AutoConfiguration 删除空 parser configuration、注释死代码、unused imports 和内部非 Bean
  factory wrappers，恢复自然 bean 方法名；Bean 名仍为
  `clientCredentialGranter`、`oAuth2AuthorizeController`；
- `DefaultClientCredentialGranter` 删除未使用 manager 字段，auto-configuration 使用二参
  constructor；旧三参 public constructor descriptor 保留并委托二参 constructor，用户既有
  `ReactiveAuthenticationHolder` 行为不变；
- `OAuth2AuthorizeControllerTest` 改用 `MockServerWebExchange`，删除重复 transport
  fixture，保留 complete/error/cancel/同步 throw 清理及 authorization_code、refresh_token
  脱敏兼容视图细粒度契约。

外部 `OAuth2AuthorizeController` 构造器、`ClientCredentialRequest`、
`ClientCredentialGranter` public API 保持兼容；未引入 Spring Security、JetLinks API
application，也未改 `AccessTokenManager` 或 Token 模型。

### 最终验证

- 定向验证：29 / 0 / 0 / 0。
  `mvn -pl hsweb-authorization/hsweb-authorization-oauth2 -am -Dsurefire.failIfNoSpecifiedTests=false -Dtest=OAuth2ServerAutoConfigurationTest,DefaultReactiveOAuth2ClientAuthenticatorTest,ClientCredentialRequestCompatibilityTest,CompositeClientCredentialGranterTest,DefaultClientCredentialGrantHandlerTest,OAuth2AuthorizeControllerTest,OAuth2TokenEndpointAutoConfigurationIntegrationTest test`
- owning module 验证：43 / 0 / 0 / 2。
  `mvn -pl hsweb-authorization/hsweb-authorization-oauth2 test`
- `git diff --check` 通过；跳过：
  `RedisAccessTokenManagerTest`、`DefaultAuthorizationCodeGranterTest`。
- 独立兼容/安全复审结论：无 P0-P2。

### 剩余验证风险

未新增 default composite -> DefaultClientCredentialGranter ->
ReactiveAuthenticationHolder 的 HTTP 成功链与 OAuth2GrantedEvent/removeToken rollback 集成测试，
以避免静态 Holder 的全局测试污染。已有 default handler 透明委托与 auto-configuration 测试；
实际 DefaultClientCredentialGranter 的 event/rollback 仍是剩余验证风险。

### 交付

- 功能提交：`1f8a4d521 feat(oauth2)!: 扩展客户端认证与凭证授权`
- Pull Request：<https://github.com/hs-web/hsweb-framework/pull/367>

## 13. v1.3 客户端类型可信配置、持久化兼容与传播

### 目标与影响范围

`clientType` 必须能够由服务端可信客户端配置指定，并在默认认证链中完整传播：

    OAuth2ClientManager
      -> OAuth2Client.clientType
      -> DefaultReactiveOAuth2ClientAuthenticator
      -> OAuth2ClientAuthentication.clientType
      -> CompositeClientCredentialGranter
      -> ClientCredentialGrantHandler

影响模块为 `hsweb-authorization-oauth2` 的客户端模型、认证上下文和测试，以及
`hsweb-system-authorization-oauth2` 的数据库客户端实体与转换逻辑。`OAuth2Client` 使用开放
字符串字段；其 getter 将历史 `null` 或空白值归一为 `default`，不改变原 Handler 路由。

### 边界与非目标

- `clientType` 只能来自 `OAuth2ClientManager` 返回的可信元数据，或由完成凭证校验的自定义
  `ReactiveOAuth2ClientAuthenticator` 显式覆盖。HTTP 的 `client_type`、`clientType` 可以作为
  普通 parameters 保留给业务，但均为不可信输入，不得参与路由；handler 必须读取认证上下文
  的 `clientType`。
- 类型是开放字符串而不是 hsweb 内置枚举，避免 hsweb 依赖 JetLinks API application 类型。
- 本阶段不改 OAuth2 grant type、AccessToken、授权码、refresh token 或权限模型。
- 不新增 TraceHolder 或 MBean；本次只传播既有请求内的客户端元数据，不增加异步、缓存、
  队列或常驻状态边界。
- EasyORM 会预初始化全部内置方言，其 PostgreSQL 方言直接引用驱动类型；
  `hsweb-commons-crud` 统一提供 `r2dbc-postgresql` runtime 依赖，下游模块不再重复声明
  test workaround。
- `OAuth2AuthorizeController` 既支持 AutoConfiguration，也兼容组件扫描。组件扫描时若已注册
  `ReactiveOAuth2ClientAuthenticator` 则使用四参数构造器，否则回退到兼容三参数构造器；
  不依赖不同模块 AutoConfiguration 的条件评估时序。

### 已实现的兼容策略

1. `OAuth2Client` 增加默认值为 `default` 的开放 `clientType` 字段，并对历史 `null`/空白值
   统一回落。
2. `OAuth2ClientAuthentication(OAuth2Client)` 从已认证客户端读取类型，脱敏客户端视图继续
   保留该非敏感字段。显式 `OAuth2ClientAuthentication(client, type, attributes)` 必须将脱敏
   client 视图覆盖为相同的 `type`，消除认证上下文与 client 兼容视图类型不一致的风险。
3. `OAuth2ClientEntity.clientType` 保持可空，不做字段初始化，也不加 `@NotBlank`；保留
   `@DefaultValue("default")`，它仅作为 EasyORM insert 的运行时默认值。`toOAuth2Client()`
   传播原始值，由 `OAuth2Client` getter 完成归一。

   自动 DDL 只会以 NativeSql 生成数据库 `DEFAULT`，不能安全地以 `nullable=false` 向已有表
   增列；缺字段 JSON 反序列化为 `null`，且 EasyORM 更新忽略 `null`，因此不会由旧客户端覆盖
   已存的显式类型。v1.3 通过可空列兼容，无需强制迁移；未做方言 SQL 迁移或数据库
   not-null 收紧。未来若需收紧约束，必须另行提供幂等迁移。
4. 旧三参数 `DefaultClientCredentialGranter` 构造器标记弃用但保留原
   `ReactiveAuthenticationManager#getByUserId` 行为；新二参数构造器继续使用
   `ReactiveAuthenticationHolder`。

### 关键代码落点

- `hsweb-authorization/hsweb-authorization-oauth2/src/main/java/org/hswebframework/web/oauth2/server/`
  下的 `OAuth2Client.java`、`authentication/OAuth2ClientAuthentication.java`、
  `authentication/DefaultReactiveOAuth2ClientAuthenticator.java`、
  `credential/DefaultClientCredentialGranter.java`、`credential/ClientCredentialRequest.java` 与
  `web/OAuth2AuthorizeController.java`。
- `hsweb-system/hsweb-system-authorization/hsweb-system-authorization-oauth2/src/main/java/org/`
  `hswebframework/web/oauth2/entity/OAuth2ClientEntity.java`。
- `hsweb-commons/hsweb-commons-crud/pom.xml`；
  `hsweb-authorization/hsweb-authorization-basic/pom.xml` 与 `hsweb-starter/pom.xml` 删除重复
  PostgreSQL test 依赖。
- `hsweb-authorization/hsweb-authorization-oauth2/src/test/java/org/hswebframework/web/oauth2/server/`
  下的 `OAuth2ClientTest.java`、`OAuth2ServerAutoConfigurationTest.java`、
  `authentication/DefaultReactiveOAuth2ClientAuthenticatorTest.java`、
  `credential/CompositeClientCredentialGranterTest.java`、
  `credential/DefaultClientCredentialGrantHandlerTest.java` 与
  `web/OAuth2TokenEndpointAutoConfigurationIntegrationTest.java`；
  `hsweb-system/hsweb-system-authorization/hsweb-system-authorization-oauth2/src/test/java/org/`
  `hswebframework/web/oauth2/entity/OAuth2ClientEntityTest.java`。

### 验证结果

已集中执行一次：

```shell
./mvnw test -pl hsweb-authorization/hsweb-authorization-oauth2,hsweb-system/hsweb-system-authorization/hsweb-system-authorization-oauth2 -am -Dtest=OAuth2ClientTest,DefaultReactiveOAuth2ClientAuthenticatorTest,CompositeClientCredentialGranterTest,DefaultClientCredentialGrantHandlerTest,OAuth2ServerAutoConfigurationTest,OAuth2TokenEndpointAutoConfigurationIntegrationTest,OAuth2ClientEntityTest -Dsurefire.failIfNoSpecifiedTests=false
```

结果为 18/18 Reactor 验证通过，39 tests，0 failures/errors/skips；`git diff --check` 通过。覆盖
默认、`null`、空白与显式类型的传播，显式认证上下文与脱敏 client 视图的一致性，HTTP 同名参数
不参与路由、secret 脱敏，以及旧三参数 granter 的认证管理器兼容行为。

CI 暴露的 EasyORM 驱动与组件扫描装配问题修复后，集中执行：

```shell
./mvnw test -q -pl hsweb-commons/hsweb-commons-crud,hsweb-authorization/hsweb-authorization-basic,hsweb-starter,hsweb-authorization/hsweb-authorization-oauth2,hsweb-system/hsweb-system-authorization/hsweb-system-authorization-oauth2
```

结果为 5 个模块共 205 tests，0 failures/errors，3 skipped；原失败上下文
`OAuth2ClientManagerAutoConfigurationTest` 已通过，`git diff --check` 通过。

### 交付

- Pull Request：<https://github.com/hs-web/hsweb-framework/pull/367>

## 14. v1.4 通用客户端认证编排

### 目标与边界

token endpoint 不再假设所有客户端认证方式都提供 `client_secret`。HTTP 认证证据提取与
客户端身份验证分成两个扩展阶段：

    HTTP request
      -> ReactiveOAuth2ClientAuthenticationRequestResolver
      -> OAuth2ClientAuthenticationRequest
      -> ReactiveOAuth2ClientAuthenticator
      -> ReactiveOAuth2ClientAuthenticationProvider
      -> OAuth2ClientAuthentication
      -> grant

本阶段继续内置 `client_secret_basic`、`client_secret_post`，并允许外部模块注册请求 Converter
和认证 Provider，以承载 JWT assertion、mTLS、AK/SK 签名或无 Secret 认证。具体 JWT、证书、
签名、PKCE 业务实现不属于 hsweb；authorization_code、refresh_token、client_credentials、
AccessTokenManager、OAuth2ClientManager 和 Token 存储模型不变，也不引入 Spring Security。

### 冻结契约

1. `OAuth2ClientAuthenticationRequest` 只保存 clientId、开放字符串 authenticationMethod、
   grantType 和不含原始凭证的安全参数，并提供 `eraseCredentials()` 生命周期方法。具体请求
   类型自行持有和清理认证证据；内置 `OAuth2ClientSecretAuthenticationRequest` 使用防御性
   `char[]` 副本保存 Secret。
2. `ReactiveOAuth2ClientAuthenticationRequestResolver` 是从 `ServerWebExchange` 和原始多值
   参数创建标准化请求的可整体覆盖门面；默认组合 Resolver 收集按 Spring `Ordered` 排序的
   `ReactiveOAuth2ClientAuthenticationRequestConverter` 贡献者并串行尝试，最后以内置 Secret
   Converter 兜底。Converter 返回 `Mono.empty()` 表示不识别当前方式；已识别但格式非法必须
   返回错误，不得回退。
3. `ReactiveOAuth2ClientAuthenticationProvider` 声明其支持的 authenticationMethod。组合
   authenticator 按方法精确选择唯一 Provider；两个外部 Provider 声明同一方法时启动失败；
   Provider 失败或返回空时认证终止，绝不尝试其他 Provider。外部 Provider 可以显式覆盖
   内置 Secret 方法。
4. Controller 只负责 grant_type 分派和编排，不解析 Basic、不校验 Secret。凭证由请求在
   认证 Publisher 的 complete、error、cancel 或同步异常终止时统一清理；grant 继续只接收
   脱敏 `OAuth2Client` 和安全参数。
5. 合法 Basic、form/query、GET/POST、Basic 优先级、现有 OAuth2 错误和三种 grant 行为保持。
   重复 client_id/client_secret、非法 Basic 等畸形输入可以按既有错误码提前拒绝，这是安全
   收紧，不构成新的认证方式。

### 自动装配与兼容

- 用户提供 `ReactiveOAuth2ClientAuthenticator` 时仍完全覆盖默认认证门面。
- 未提供认证门面时，自动装配组合 authenticator，包含外部 Provider 和内置 Secret Provider。
- 用户提供 `ReactiveOAuth2ClientAuthenticationRequestResolver` 时整体覆盖默认解析门面；否则
  自动装配组合 Resolver，收集外部 Converter 并以内置 Secret Converter 兜底。
- PR 内尚未发布的具体请求 DTO 直接收敛为最终类型化模型，不为中间形态保留适配分支；PR
  之前已经存在的 OAuth2ClientManager、OAuth2Client、Granter 和 Controller 三参数构造方式
  继续兼容。

### 验证目标

- 回归合法 Basic、form、GET/POST、authorization_code、refresh_token 和 client_credentials。
- 验证 Converter 顺序、非法输入不回退、重复敏感参数、Provider 唯一路由、未知方式和失败不
  回退。
- 验证 Secret 在 complete、error、cancel 和同步异常后清理，且 grant、attributes、事件不含
  原始凭证。
- 使用无 Secret 的自定义 Header 请求做 HTTP 集成测试，证明扩展不需要修改 Controller；
  不在测试中伪造完整 JWT、PKCE 或 mTLS 业务实现。

### 验证结果与剩余风险

- 定向认证编排与 HTTP 集成测试：30 tests，0 failures/errors/skips。
  `mvn test -pl hsweb-authorization/hsweb-authorization-oauth2 -am -Dtest=OAuth2ServerAutoConfigurationTest,DefaultReactiveOAuth2ClientAuthenticatorTest,ClientSecretAuthenticationRequestConverterTest,CompositeReactiveOAuth2ClientAuthenticationRequestResolverTest,CompositeReactiveOAuth2ClientAuthenticatorTest,OAuth2AuthorizeControllerTest,OAuth2TokenEndpointAutoConfigurationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false`
- `hsweb-authorization-oauth2` owning module 全量测试：53 tests，0 failures/errors，2 skipped。
  `mvn test -pl hsweb-authorization/hsweb-authorization-oauth2`
- 安装当前 reactor 构件后执行 `hsweb-system-authorization-oauth2` owning module 全量测试：
  8 tests，0 failures/errors/skips；`OAuth2ClientManagerAutoConfigurationTest` 通过，覆盖组件扫描下
  五参数 Controller 构造器装配。
- 从根模块使用 `-am` 执行系统模块全链路测试时，被目标模块之前的既有
  `DefaultSwitcherTest` 阻断；该用例单独运行仍稳定失败，目标目录无本次改动。本阶段未修改
  无关 datasource 代码，OAuth2 两个 owning module 已分别完成全量验证。

### 交付

- Pull Request：<https://github.com/hs-web/hsweb-framework/pull/367>

## 15. v1.5 Token 响应与认证结果标准化

### 目标与影响范围

在不改变既有 OAuth2 grant、Token 存储和端点编排的前提下，补齐两项通用协议语义：

1. `AccessToken` 显式返回标准 `token_type` 和可选 `scope`；
2. `OAuth2ClientAuthentication` 显式保留本次认证使用的 `authenticationMethod`，使同一
   clientType 下的 Secret、JWT assertion、mTLS 等认证方式可以被 grant policy 和审计可靠区分。

影响范围仅限 `hsweb-authorization-oauth2` 的响应 DTO、客户端认证结果、默认 Secret Provider
及对应单元/HTTP 集成测试。不修改 `AccessTokenManager`、Redis 数据结构、
`OAuth2GrantService`、Controller grant 枚举或 JetLinks 业务模型。

### 兼容边界

- 保留 `AccessToken()` 和已发布的 `AccessToken(String accessToken, String refreshToken,
  int expiresIn)`；旧构造方式新增默认 `token_type=Bearer`，不要求调用方迁移。
- `scope` 使用 OAuth2 标准的空格分隔字符串；未设置时不输出该字段，不从 hsweb
  `Authentication` 或请求参数隐式推导。
- 保留 `OAuth2ClientAuthentication(OAuth2Client)` 和
  `OAuth2ClientAuthentication(OAuth2Client, String, Map)`；新增包含
  `authenticationMethod` 的构造器。旧程序化调用的 method 允许为空，避免伪造认证事实。
- 默认 Secret Provider 从已验证的 `OAuth2ClientAuthenticationRequest` 传播 method；自定义
  Provider 应使用新构造器返回自己的开放方法标识。
- `credentialRef`、授权版本等仍属于安全 attributes，不提升为 hsweb 持久化字段。
- `OAuth2Response.parameters` 本阶段不改变序列化形态；错误响应标准化和通用 grant handler
  registry 会改变更大的端点契约，留待独立设计和验证。

### 实施步骤与验证

1. 调整 `AccessToken` 构造器、字段和序列化注解，并补 DTO 序列化测试。
2. 调整 `OAuth2ClientAuthentication` 构造器与契约，由默认 Secret Provider 传播 method。
3. 扩充认证 Provider、Controller 和 HTTP 集成测试，覆盖 Basic/Post method 传播、旧构造器
   兼容、Token 默认类型及 scope 输出。
4. 阶段完成后统一执行 `hsweb-authorization-oauth2` 定向测试和 owning module 全量测试，再将
   结果回填本节。

风险：新增 `token_type` 是成功响应的兼容性增量；极少数对 JSON 字段集合做严格相等判断的
客户端需要调整。`scope` 仅在显式设置时输出，避免改变现有响应。未新增缓存、队列、异步边界
或用户可见错误，因此不需要 TraceHolder、MBean 和 i18n 资源。

### 实现与验证结果

- `AccessToken` 保留 no-arg 和三参数构造器，新增五参数构造器；默认及空白
  `tokenType` 均归一为 `Bearer`，`scope` 为空时不参与 JSON 序列化。
- `OAuth2ClientAuthentication` 保留两个既有构造器，新增包含认证方法的构造器；默认 Secret
  Provider 从已完成方法路由的请求传播 `client_secret_basic` 或 `client_secret_post`。
- HTTP 集成测试验证旧 GET Basic 和 POST form/custom provider 均返回顶层 `token_type`，仅显式
  scope 返回顶层 `scope`，同时保留 Secret 脱敏与 clientType 精确路由。
- 定向验证：15 tests，0 failures/errors/skips。
  `mvn test -pl hsweb-authorization/hsweb-authorization-oauth2 -am -Dtest=AccessTokenTest,DefaultReactiveOAuth2ClientAuthenticatorTest,OAuth2TokenEndpointAutoConfigurationIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false`
- owning module 全量验证：55 tests，0 failures/errors，2 skipped。
  `mvn test -pl hsweb-authorization/hsweb-authorization-oauth2`
- 收紧 HTTP 断言后的最终复验：9 tests，0 failures/errors/skips。
  `mvn test -pl hsweb-authorization/hsweb-authorization-oauth2 -Dtest=AccessTokenTest,OAuth2TokenEndpointAutoConfigurationIntegrationTest`
- `javap -public` 确认 `AccessToken(String,String,int)`、`AccessToken()` 以及
  `OAuth2ClientAuthentication` 两个既有构造器 descriptor 均保留；`git diff --check` 通过。
- 未新增生产或测试依赖。尝试使用 `WebTestClient.jsonPath` 时确认当前模块未引入 Jayway
  JsonPath，已改回直接验证真实 HTTP JSON 字节，不为两项字段断言扩大依赖。
- Commit：本次提交；Pull Request：<https://github.com/hs-web/hsweb-framework/pull/367>。
