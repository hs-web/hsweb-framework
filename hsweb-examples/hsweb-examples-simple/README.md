# 演示
简单示例,无页面

## 运行

1. IDE
导入项目后,运行: `org.hswebframework.web.example.simple.SpringBootExample.main`

2. maven
先安装 `hsweb-framework`
```bash
 /hsweb-framework $ mvn install
 /hsweb-framework $ cd hsweb-examples/hsweb-examples-simple
 /hsweb-examples-simple $ mvn spring-boot:run
  
```

3. 测试
使用Postman之类的http测试工具请求: 
```bash
  # 登录
  HTTP POST : http://localhost:8081/authorize/login?username=admin&password=admin
  # 测试数据权限控制-查询
  HTTP GET :  http://localhost:8081/test/testQuery
```

如果不想使用权限控制,请注释掉 pom.xml的
```xml
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-authorization-shiro</artifactId>
    <version>${project.version}</version>
</dependency>

```
