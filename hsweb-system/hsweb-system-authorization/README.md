## 权限功能模块

1. 提供用户,角色管理,登录授权等功能
2. 提供统一的多维度,可拓展的权限分配

        权限设置不再像以往那样和角色,用户直接关联.在hsweb里,权限设置是通用的.
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


## 授权登录接口
http接口: `POST /authorize/login`, 登录接口支持2种`content-type`,`application/json`(Json RequestBody方式)和`application/x-www-form-urlencoded`(表单方式),
请在调用等时候指定对应等`content-type`.必要参数: `username` 和 `password`.

⚠️注意: 此接口只实现了简单的登录逻辑,不过会通过发布各种事件来实现自定义的逻辑处理.

1. `AuthorizationDecodeEvent` 在接收到登录请求之后触发,如果在登录前对用户名密码进行里加密,可以通过监听此事件实现对用户名密码的解密操作
2. `AuthorizationBeforeEvent` 在`AuthorizationDecodeEvent`事件完成后触发,可通过监听此事件并获取请求参数,实现验证码功能
3. `AuthorizationSuccessEvent` 在授权成功后触发.注意: 权限控制模块也是通过监听此事件来完成授权
4. `AuthorizationFailedEvent` 授权失败时触发.当发生过程中异常时触发此事件

什么? 还不知道如何监听事件? [快看这里](https://github.com/hs-web/hsweb-framework/wiki/事件驱动)


## 权限设置

TODO
