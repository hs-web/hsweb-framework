# 简单的jwt权限拓展

登录时，传入参数: token_type=jwt
```bash
    $ POST http://localhost:8081/authorize/login?username=admin&password=admin&token_type=jwt
```
返回jwt token
```json
{
    "result": {
        "userId": "f947788cd922f16a9e58727e13e4b806",
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJ0ZXN0IiwiaWF0IjoxNTA0MTYxNDM2LCJzdWIiOiJ7XCJ0b2tlblwiOlwiZDU1MmVjZDgyZGFjY2EwMWJiZWI3ZmMxNmU2NmQ1OTNcIixcInVzZXJJZFwiOlwiZjk0Nzc4OGNkOTIyZjE2YTllNTg3MjdlMTNlNGI4MDZcIn0iLCJleHAiOjE1MDQxNjUwMzZ9.LP7Eb0cqmpbMXBjM7yPM0vZ8T3tDd3Zmme3j-e3HTvs",
    },
    "status": 200,
    "timestamp": 1504161444051
}
```

在调用api时,设置http header:
```bash
    Authorization: jwt {登录时获取的token}
```

## 自定义jwt 密钥
使用base64生成密钥如: 
```java
Base64.encodeBase64String("密钥内容".getBytes())
```

修改application.yml
```yaml
hsweb:
    authorize:
      jwt:
        id: your_jwt_id
        secret: 上一步生成的base64密钥
```
