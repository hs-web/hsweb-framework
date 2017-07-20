# 通用服务类
 提供通用增删改查服务
 
## DSL查改删
查询,实现`DefaultDSLQueryService`接口
```java
    // select * from user where name = ? limit 0,1
   createQuery().where("name","张三").single();
```
```java
    // select * from user where name = ? or name = ?
   createQuery().where("name","张三").or().is("name","李四").list();
```

```java
    //select * from user where name = ? and (age> ? and age <?)
   createQuery().where("name","张三").nest().gt("age",10).or().lt("age",20).end().list();
```

修改,实现`DefaultDSLUpdateService`接口
```java
    // update user set ... where name = ?
   createUpdate(data).where("name","张三").exec();
    //不会修改为null的属性,
```

```java
    // update user set name=?,age=? where name = ?
   createUpdate(data).include("name","age").where("name","张三").exec();
```

```java
    // update user set name=? where name = ?
   createUpdate().set("name","新张三").where("name","张三").exec();
```

删除,实现`DefaultDSLDeleteService`接口
```java
//delete from user where name = ?
createDelete().where("name","张三").exec();
```

查改删,条件使用的方式都相同.