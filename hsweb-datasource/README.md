# 动态数据源模块
提供动态数据源支持功能,支持注解方式,编程方式动态切换数据源,支持事务中切换数据源,支持跨数据库事务

目前提供JTA实现,请看:[hsweb-datasource-jta](hsweb-datasource-jta)

# example

表达式方式:

application.yml配置
```xml
hsweb:
    datasource:
        switcher:
           test: # 只是一个标识
              # 拦截类和方法的表达式
              expression: org.hswebframework.**.*Service.find*
              # 使用数据源
              data-source-id: read_db
```

编程方式:
```java
  //切换到 id为mysql_read_01的数据源
  DataSourceHolder.switcher().use("mysql_read_01");
  // ....
  //切换到 id为mysql_write_01的数据源
  DataSourceHolder.switcher().use("mysql_write_01");
  // ....
  // 切换到上一次使用的数据源 (mysql_read_01)
   DataSourceHolder.switcher().useLast();
  // ...
  // 切换到默认的数据源
  DataSourceHolder.switcher().useDefault();
```

注解方式:
```java
@UseDataSource("mysql_write_01")
String insert(MyEntity);
 
@UseDataSource("mysql_read_01")
MyEntity selectByPk(String id);
 
@UseDefaultDataSource()
MyEntity selectByPk(String id);
```

注意: 如果没有使用`hsweb-datasource-jta`模块,则无法在事务中切换数据源,
你可能需要先取消掉对应方法上的事务:如在方法上注解`@Transactional(propagation = Propagation.NOT_SUPPORTED)`