# 动态数据源模块
提供动态数据源支持功能,支持注解方式,编程方式动态切换数据源,支持事务中切换数据源,支持跨数据库事务

目前提供JTA实现,请看:[hsweb-datasource-jta](hsweb-datasource-jta)

# example

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

