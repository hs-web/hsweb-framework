# 机构权限控制
实现基于组织机构的权限信息获取,以及数据权限控制

### 获取当前用户对应的人员信息

```java
   PersonnelAuthentication person= PersonnelAuthentication.current().orElse(null);
    
    //人员基本信息
   person.getPersonnel();
   //人员的全部职位信息
   person.getPositions();
   //人员的关系信息,用于判断人与人,人与物的关系
   person.getRelations();
   //更多方法请查看源代码
```

### 数据权限控制

约定:
1. 通过在方法上注解: `@Authorize(dataAccess=@RequiresDataAccess)` 开启数据权限控制
2. 分页查询: 仅支持使用通用查询条件(`@QueryParamEntity`)作为参数的方法.
3. 根据主键,修改,删除: 仅支持实现`QueryController`的类
4. 对`行政区划`进行权限控制的实体需实现接口: `DistrictAttachEntity`
5. 对`机构`进行权限控制的实体需实现接口: `OrgAttachEntity`
6. 对`部门`进行权限控制的实体需实现接口: `DepartmentAttachEntity`
7. 对`岗位`进行权限控制的实体需实现接口: `PositionAttachEntity`
8. 对`人员`进行权限控制的实体需实现接口: `PersonAttachEntity`
注意,具体的控制规则配置是由`hsweb-system-authorization`模块实现

### 自定义控制

你可以参照包`org.hswebframework.web.organizational.authorization.simple.handler`中的实现
进行自定义控制