## 组织架构管理
提供 机构-部门-职位-人员 的组织架构管理


## SQL条件
`hsweb-system-organizational-local`模块中提供了一些自定义的查询条件,用于对组织机构关联查询.可以在动态查询中
进行使用,例如:

```xml

//查询orgId为1234的机构以及其所有子机构的数据
createQuery().where("orgId","org-child-in","1234").list();

```

1. dist-child`(-not)`-in : 参数`(不)`在指定的行政区域以及子节点中
2. org-child`(-not)`-in : 参数`(不)`在指定的机构以及子节点中
3. dept-child`(-not)`-in: 参数`(不)`在指定的部门以及子节点中
3. pos-child`(-not)`-in: 参数`(不)`在指定的岗位以及子节点中
4. person`(-not)`-in-position: 人员ID`(不)`在岗位中
5. user`(-not)`-in-position: 用户ID`(不)`在岗位中
