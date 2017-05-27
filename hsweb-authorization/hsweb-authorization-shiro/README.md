# shiro 权限控制实现

[shiro官方文档](http://shiro.apache.org/documentation.html)

本模块对shiro进行拓展,增加对[hsweb-authorization-api](../hsweb-authorization-api)中的注解进行实现。
实现类如下:

| 注解名称       | 实现类       | 
| ------------- |:-------------:| 
| [`@Authorize`](../hsweb-authorization-api/src/main/java/org/hswebframework/web/authorization/annotation/Authorize.java)    |      [ExpressionAnnotationMethodInterceptor](src/main/java/org/hswebframework/web/authorization/shiro/boost/SimpleAuthorizeMethodInterceptor.java)        |
| [`@RequiresExpression`](../hsweb-authorization-api/src/main/java/org/hswebframework/web/authorization/annotation/RequiresExpression.java)    | [ExpressionAnnotationMethodInterceptor](src/main/java/org/hswebframework/web/authorization/shiro/boost/ExpressionAnnotationMethodInterceptor.java)      | 
| [`@RequiresDataAccess`](../hsweb-authorization-api/src/main/java/org/hswebframework/web/authorization/annotation/RequiresDataAccess.java)    | [DataAccessAnnotationMethodInterceptor](src/main/java/org/hswebframework/web/authorization/shiro/boost/DataAccessAnnotationMethodInterceptor.java)      | 

## 拓展接口

### 行级权限控制器

控制逻辑简述:

1. 获取被拦截方法的注解信息,取得当前需要验证的permission,action。如: user,query
2. 根据上一步获取到需要验证的permission和action获取当前登录用户权限信息中配置的控制规则（控制规则可以在前端进行设置）
3. 调用控制器进行验证

可自己实现DataAccessHandler接口并注入spring以实现自定义的控制方式

内置的控制方式

1. [CustomDataAccessHandler](src/main/java/org/hswebframework/web/authorization/shiro/boost/handler/CustomDataAccessHandler.java) 自定义控制器
2. [OwnCreatedDataAccessHandler](src/main/java/org/hswebframework/web/authorization/shiro/boost/handler/OwnCreatedDataAccessHandler.java) 控制只能操作自己创建的数据
3. [ScriptDataAccessHandler](src/main/java/org/hswebframework/web/authorization/shiro/boost/handler/ScriptDataAccessHandler.java) 使用脚本方式控制
4. [FieldScopeDataAccessHandler](src/main/java/org/hswebframework/web/authorization/shiro/boost/handler/FieldScopeDataAccessHandler.java) 控制字段的值范围,如: orgId in (1,2,3,4)
5. [FieldFilterDataAccessHandler](src/main/java/org/hswebframework/web/authorization/shiro/boost/handler/FieldFilterDataAccessHandler.java) 控制字段的操作范围,此控制器替代之前的FieldAccess功能


注意: 控制需满足的条件请查看控制器源代码查看注释获取
