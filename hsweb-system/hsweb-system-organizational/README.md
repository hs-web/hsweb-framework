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
5. user`(-not)`-in-position`(-child)`: 用户ID`(不)`在岗位中`(包含子级岗位)`
6. user`(-not)`-in-department`(-child)`: 用户ID`(不)`在部门中`(包含子级岗位)`
7. user`(-not)`-in-org`(-child)`: 用户ID`(不)`在机构中`(包含子级岗位)`
8. user`(-not)`-in-dist`(-child)`: 用户ID`(不)`在行政区域中`(包含子级岗位)`
9. person`(-not)`-in-position`(-child)`: 人员ID`(不)`在岗位中`(包含子级岗位)`
10. person`(-not)`-in-department`(-child)`: 人员ID`(不)`在部门中`(包含子级岗位)`
11. person`(-not)`-in-org`(-child)`: 人员ID`(不)`在机构中`(包含子级岗位)`
12. person`(-not)`-in-dist`(-child)`: 人员ID`(不)`在行政区域中`(包含子级岗位)`

注意: 括号中的内容是可选的