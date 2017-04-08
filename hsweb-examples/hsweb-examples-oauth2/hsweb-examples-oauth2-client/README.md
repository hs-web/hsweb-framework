# hsweb OAuth2.0 客户端演示

1. 运行`OAuth2ClientApplication.main`启动项目,启动前先保证[服务端](../hsweb-examples-oauth2-server)已启动
2. 访问 `http://localhost:8808/login.html?redirect=/test` ,点击使用hsweb登录引导到服务端
3. 登录成功后会自动跳转到 `http://localhost:8808/test`,显示`admin角色`相关数据则代表运行成功