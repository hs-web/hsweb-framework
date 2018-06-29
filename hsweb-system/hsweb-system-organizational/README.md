## 组织架构管理
提供 机构-部门-职位-人员 的组织架构管理


## SQL条件
`hsweb-system-organizational-local`模块中提供了一些自定义的查询条件,用于对组织机构关联查询.可以在动态查询中
进行使用,例如:

```java

//查询orgId为1234的机构以及其所有子机构的数据
createQuery().where("orgId","org-child-in","1234").list();

```

1. dist-child`(-parent)(-not)`-in : 参数`(不)`在指定的行政区域以及子(父)节点中
2. org-child`(-parent)(-not)`-in : 参数`(不)`在指定的机构以及子(父)节点中
3. dept-child`(-parent)(-not)`-in: 参数`(不)`在指定的部门以及子(父)节点中
3. pos-child`(-parent)(-not)`-in: 参数`(不)`在指定的岗位以及子(父)节点中
4. user`(-not)`-in-position`(-child)(-parent)`: 用户ID`(不)`在岗位中`(包含子级(父级)岗位)`
5. user`(-not)`-in-department`(-child)(-parent)`: 用户ID`(不)`在部门中`(包含子级(父级)岗位)`
6. user`(-not)`-in-org`(-child)(-parent)`: 用户ID`(不)`在机构中`(包含子级(父级)岗位)`
7. user`(-not)`-in-dist`(-child)(-parent)`: 用户ID`(不)`在行政区域中`(包含子级(父级)岗位)`
8. person`(-not)`-in-position`(-child)(-parent)`: 人员ID`(不)`在岗位中`(包含子级(父级)岗位)`
9. person`(-not)`-in-department`(-child)(-parent)`: 人员ID`(不)`在部门中`(包含子级(父级)岗位)`
10. person`(-not)`-in-org`(-child)(-parent)`: 人员ID`(不)`在机构中`(包含子级(父级)岗位)`
11. person`(-not)`-in-dist`(-child)(-parent)`: 人员ID`(不)`在行政区域中`(包含子级(父级)岗位)`

注意: 括号中的内容是可选的
