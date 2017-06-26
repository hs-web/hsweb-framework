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

启动成功后,浏览器打开: [swagger-ui](http://localhost:8080/swagger-ui.html) 试试

如果不想使用权限控制,请注释掉 pom.xml的
```xml
<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-authorization-shiro</artifactId>
    <version>${project.version}</version>
</dependency>

```
