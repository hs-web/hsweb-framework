## 系统启动模块

整合系统常用的启动方式,如spring-boot自动配置,自动维护表结构.启动执行脚本等等

## 应用 JSON 门面

直接依赖 `json-facade-spring`，由其自动配置提供 `org.jetlinks.json.JsonMapper` Bean，基于应用
`ObjectMapper` 的配置快照，保留已注册 Module、命名规则和注解。业务可直接注入使用，
也可声明自己的 `JsonMapper` Bean 覆盖默认值。快照不跟踪原生 mapper 后续变更，应在启动时完成配置。

默认同时注册到 `Json.global()`，不修改 HTTP 编解码器；多个独立或重复的 ApplicationContext
设置 `json.facade.global-enabled=false` 并注入各自 Bean。全局实例不会随容器关闭而重置。
需要完全关闭自动配置时设置 `json.facade.enabled=false`。
框架默认入口直接使用 facade 的全局配置，模块元数据读取使用一次性局部实例。
Spring 全局注册前不要调用默认入口；原生 mapper/customizer 的构造中不能反向使用全局。
独立上下文中的认证构建器显式传入各自 Bean。
EasyORM 的持久化 mapper 单独管理，HTTP 的字段脱敏、权限或输出包装不会自动进入数据库。
