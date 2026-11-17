# 权限控制API
用于权限控制的API接口,支持RBAC权限控制,支持数据级（控制到行,列）权限控制.

[用户令牌管理](token.md)

[权限控制配置](define.md)

# 介绍

以下讲到的类都是基于包:org.hswebframework.web.authorization

### 常用注解:
_点击名称,查看源代码注释获得使用说明_

| 注解名称       | 说明          | 
| ------------- |:-------------:| 
| [`@Authorize`](src/main/java/org/hswebframework/web/authorization/annotation/Authorize.java)    | RBAC方式权限控制注解 | 

[自定义数据权限控制](custom-data-access.md)

### JSON 认证构建

`SimpleAuthenticationBuilder` 默认使用 facade 的 `Json.global()`；可通过新增的构造参数传入
`JsonMapper`，`SimpleAuthenticationBuilderFactory`、`SimpleDataAccessConfigBuilderFactory`
也支持相同配置入口。

```java
JsonMapper mapper = Json.global(); // Spring 中也可直接注入应用的 JsonMapper
var factory = new SimpleDataAccessConfigBuilderFactory(mapper);
factory.init(); // 脱离 Spring 手动创建时，注册默认转换器
var builder = new SimpleAuthenticationBuilder(factory, mapper);
builder.permissionJson(mapper.wrap(List.of(Map.of("id", "device", "actions", List.of("query")))));
Authentication authentication = builder.build();
```

已有权限 JSON 可以直接使用 `builder.permissionJson(mapper.readTree(permissionJson).asArray())`。
设置列表使用 `readAsList`，JSON 对象与数据权限配置使用 `readAsMap`；认证属性按 `Serializable`
值类型直接绑定。角色读取复用缓存的完整 `JsonType`，避免高频调用重复构造集合类型。

旧 `permission(JSONArray)`、`dimension(JSONArray)` 保留并弃用，对应替代为
`permissionJson(JsonArray)`、`dimensionJson(JsonArray)`。String 和完整认证 JSON 入口继续调用
旧数组扩展点，已有子类覆写仍生效。构建器按次创建，mapper 可共享。
数据权限对象直接交给 `fromMap`；自定义 `DataAccessConfigConverter` 仍接收 String，
只有该边界需要将嵌套 `config` 编码，未设置 `config` 时编码完整权限配置。

### 常用类
_点击名称,查看源代码注释获得使用说明_


| 类名       | 说明          | 
| ------------- |:-------------:| 
| [`Authentication`](src/main/java/org/hswebframework/web/authorization/Authentication.java)    | 用户的认证信息 | 
| [`AuthenticationHolder`](src/main/java/org/hswebframework/web/authorization/AuthenticationHolder.java)      | 用于获取当前登录用户的认证信息      | 


### Listener
可以使用Spring的`@EventListener`监听授权事件
来进行授权逻辑拓展，在授权前后执行可自定义的操作.如rsa解密帐号密码,验证码判断等。

默认事件列表():

| 类名       | 说明          | 
| ------------- |:-------------:| 
| [`AuthorizationDecodeEvent`](src/main/java/org/hswebframework/web/authorization/events/AuthorizationDecodeEvent.java)    | 接收到请求参数时 | 
| [`AuthorizationBeforeEvent`](src/main/java/org/hswebframework/web/authorization/events/AuthorizationBeforeEvent.java)      | 验证密码前触发      | 
| [`AuthorizationFailedEvent`](src/main/java/org/hswebframework/web/authorization/events/AuthorizationFailedEvent.java)      | 授权验证失败时触发      | 
| [`AuthorizationSuccessEvent`](src/main/java/org/hswebframework/web/authorization/events/AuthorizationSuccessEvent.java)      | 授权成功时触发      | 
| [`AuthorizationExitEvent`](src/main/java/org/hswebframework/web/authorization/events/AuthorizationExitEvent.java)      | 用户注销时触发      | 

例子:

```java
@Component
public class CustomAuthorizationSuccessListener {
        @EventListener
        public void on(AuthorizationSuccessEvent event) {
            Authentication authentication=event.getAuthentication();
            //....
            System.out.println(authentication.getUser().getName()+"登录啦");
        }
}
```

