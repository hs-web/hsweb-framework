# 日志模块

## 访问日志 API

controller类或者方法上,注解 `@AccessLogger("功能描述")`


## 开启访问日志
引入依赖,`hsweb-access-logging-aop`,在启动类中注解`@EnableAccessLogger`.

自定义日志监听,创建类,实现: ``AccessLoggerListener``接口并注入到spring容器,
当有日志产生时,会调用接口方法`onLogger`,并传入日志信息

