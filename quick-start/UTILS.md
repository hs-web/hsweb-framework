# 实用工具包

hsweb提供了一些实用的工具类

1. 实体类属性拷贝

```java
//拷贝source的属性到target
FastBeanCopier.copy(source,target);
//id和createTime属性不拷贝
FastBeanCopier.copy(source,target,"id","createTime");
//只拷贝name和age属性
FastBeanCopier.copy(source,target,FastBeanCopier.include("name","age"));

```

2. 枚举数据字典,[点击查看](../hsweb-core/README.md#数据字典])

