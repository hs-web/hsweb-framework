
# 1.查询：
```java
    
    import static MyBean.Property.*; //属性名
    myService.createQuery()
        .where(name,"admin")
        .or(name,"root")
        .list(); //list(), list(0,10), single(),total();
    //or
    myService.createQuery().fromBean(myBean)
        .where(name)
        .or(name)
        .list(); 
    
    // 复杂查询条件
    // 等同sql  where name is not null and (name like '李%' or name like '周%') and age >0
    // 参数全部预编译,不用担心注入
     myService.createQuery()
        .where().notNull(name)
        .nest().or().like$(name,"李").or().like$(name,"周").end()
        .and().gt(age,10).list();
     
    //自定义sql条件
    myService.createQuery()
        .where()
        .and().sql("name !=''")
        .or().sql("age < #{age}",{age:10})// 使用预编译方式
        .or().sql("age = #{[0]}",Arrays.asList(20))//获取集合参数
        .or().sql("age > ? and (age <?)",60,100)//使用参数列表方式
        .list(); 
```

# 2.修改,支持和query一致的条件
```java
    import static MyBean.Property.*;
    myService.createUpdate()
        .set(status,1)
        .where(id,"data-id").exec();
    // or
    myService.createUpdate(myBean).fromBean().where(id).exec();
```

# 3.删除,支持和query一致的条件
```java
    import static MyBean.Property.*;
    myService.createDelete().where(id,"data-id").exec();
```