# 通用CRUD使用手册

hsweb提供了通用增删改查功能,并实现使用mybatis和easyorm实现了动态条件,支持dsl方式构造查询条件,
前端可直接传递动态参数到后端,实现灵活的条件查询,并且全部条件使用预编译,不存在sql注入问题.


## 使用
在[入门教程](README.md#增删改查)中已经创建好了一个增删改查的功能.

1. 通用Dao

通用Dao实现了动态增删改查,是整个通用增删改查的基础,现阶段只提供了Mybatis实现,为了灵活性,Mybatis仍然使用xml配置方式.
[查看完整的Dao说明](../hsweb-commons/hsweb-commons-dao)

2. 通用Service
通用Service提供了基于通用Dao的Service功能,对常用的方法进行了封装,并且提供了dsl方式的删改查方法.

继承了`GenericEntityService`接口即可使用.

```java
   public List<TestEntity> queryByNameAndStatus(String name,byte status){
        // 等同于 where name =? and status =?
        return createQuery()
                .where("name",name)
                .and("status",status)
                .listNoPaging();
   }
```

更多用法:
```java
    /*  查询  */
    //where name=? or name = ?
    createQuery()
        .where("name",name1).or("name",name2)
        .listNoPaging();

    //where name like ? limit 0,10
    createQuery().where().like("name",?).list(0,10);
    
    //where name like ? and (age<? or age>?)
    createQuery().where()
            .like("name",name)
            .nest()
                .lt("age",ageLt).or().gt("age",ageGt)
            .end()
            .list(); //默认等同于list(0,20)
  
    //where name=? or name=?
    createQuery().where()
            .sql("name=?",name)
            .or()
            .sql("name=#{name}",paramObject)
            .single();//limit 0,1
            
   // where status in(1,2,3) and ( name like ? or name like ? or name like ?)
    createQuery()
            .when(status==1,query->query.in("status",1,2,3))
            .nest()
            .each("name",nameList,query->query.or()::$like$)
            .end()
            .list();
    
   /* 修改 条件支持与查询一致*/
   //set status=? where id = ?
   //注意最后的exec()方法.
   createUpdate().set("status",1).where("id",id).exec();
   //修改实体类中不为null的属性
   createUpdate(entity).where("id",id).exec();
      
   /* 删除 条件支持与查询一致*/
   //注意最后的exec()方法.
   createDelete().where("id",id).exec();
```

如果想调用其他dao进行dsl操作,可使用`DefaultDSLQueryService.createQuery(dao)`
,`DefaultDSLUpdateService.createUpdate(dap)`
,`DefaultDSLDeleteService.createDelete(dao)` 进行操作


默认支持的条件类型方法:is(eq),not,in,notIn,isNull,notNull,like,notLike,lt,gte,lte,等等.

还可以[自定义的通用的查询条件](../hsweb-commons/hsweb-commons-dao/hsweb-commons-dao-mybatis/README.md#拓展动态条件): 

以在组织架构模块[中自定义的查询条件](../hsweb-system/hsweb-system-organizational/README.md#SQL条件)为例:

```java
//user-in-org为自定义的查询条件类型
//userId字段为指定机构中的用户的数据
createQuery()
    .where("userId","user-in-org",orgId)
    .list();
```
这样就无需在需要用到相关查询的地方重复的编写mybatis mapper xml.

3. 通用Controller

参照[这里](../hsweb-commons/hsweb-commons-controller/README.md)
