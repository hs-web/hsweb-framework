#OAuth2 模块
目前仅实现了``client_credentials``方式调用

## 1.引入依赖
(引入``hsweb-web-starter``依赖的可忽略此步骤)
```xml
<dependency>
    <groupId>org.hsweb</groupId>
    <artifactId>hsweb-web-oauth2-simple</artifactId>
</dependency>
```

## 2.服务端
在可进行oauth2方式调用的接口上注解 ``@Authorize(api = true)``
如果已有注解，直接加上 ``api=true``属性即可

登录系统，在接口管理-客户端管理中建立客户端并绑定用户(api的权限即为绑定用户的权限)
## 3.客户端
1、申请``access_token``

POST请求: ``http://[host]:[port]/oauth2/access_token``

参数： ``grantType=client_credentials&client_id={客户端ID}&client_secret={客户端密钥}``

返回: 
```json
{
  "access_token": "{授权码}",
  "token_type": "bearer",
  "expires_in": 3600,
  "scope": "public"
}
```
2、请求接口:``http://[host]:[port]/api/yourApi?access_token={申请到的授权码}``
