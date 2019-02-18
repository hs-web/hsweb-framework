# 应用基本信息配置

```yaml
hsweb:
    app:
      name: my-application
      comment: 我的应用
      version: 1.0.0
      auto-init: true # 启动服务时进行初始化(执行classpath*:/hsweb-starter.js)
```

# 跨域设置
修改application.yml
```yaml
hsweb: 
    cors:
      enable: true
      configs:
        - /**:
            allowed-headers: "*"
            allowed-methods: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
            allowed-origins: ["http://xxx.example.com"]
            allow-credentials: true
            maxAge: 1800
```

# json序列化配置

```yaml
fastjson:
    features: WriteNullListAsEmpty,WriteNullNumberAsZero,WriteNullBooleanAsFalse
```
