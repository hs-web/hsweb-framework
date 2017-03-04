## 权限功能模块

提供用户,角色,权限管理

## 授权
[AuthorizationController](hsweb-system-authorization-controller/src/main/java/org/hswebframework/web/controller/authorization/AuthorizationController.java)
仅进行基础授权,通过触发`AuthorizationListener`,进行自定义控制逻辑.详细方式见:[hsweb-authorization-api](../../hsweb-authorization/hsweb-authorization-api)