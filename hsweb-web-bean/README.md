## 实体类模块,通用bean,po,和自定义验证器

### 目录结构

```bash
--------src/main/java
---------------------org.hsweb.web.bean
----------------------------common      # 通用bean,如增删改查通用参数
------------------------------po        # 各个功能的po实体
----------------------------validator   # 自定义hibernate-validator
-----------------resources/system
---------------------------------install.sql #首次启动时执行的sql
```

### 说明
po对象都应该继承[GenericPo](src/main/java/org/hsweb/web/bean/po/GenericPo.java)
GenericPo 的泛型为主键的类型,hsweb建议使用String类型,通过createUID()方法手动生成id
