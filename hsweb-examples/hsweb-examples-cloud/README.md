# spring-cloud 示例

```bash
----------hsweb-examples-cloud
--------------hsweb-examples-cloud-gateway          #服务注册,路由.api的入口
--------------hsweb-examples-cloud-service01        #1号测试服务,可从用户中心获取当前登录用户并进行权限控制
--------------hsweb-examples-cloud-user-center      #用户中心,用于用户登录授权
```

# 启动

## 1. main方法启动
1. 执行`hsweb-examples-cloud-gateway`模块中的 `GateWayApplication`
2. 执行`hsweb-examples-cloud-service01`模块中的 `Service01Application`
3. 执行`hsweb-examples-cloud-user-center`模块中的 `UserCenterApplication`

##2. 使用spring-boot插件启动

分别进入3个模块,执行`mvn spring-boot:run`

## 2. docker

1. 执行 `./build-docker.sh` 构建docker镜像

2. 执行 'docker-compose up' 等待服务启动完成

# 访问
访问: http://localhost:8761/

# 测试

1. 使用 `PostMan` 之类的工具发起POST请求:

    http://localhost:8761/api/user-center/authorize/login?username=admin&password=admin&token_type=jwt

拿到结果如下:
```json
{
    "result": {
        "userId": "b3d4ee054b8195e8ce2dbecedefbfb49",
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJoc3dlYi1qd3QiLCJpYXQiOjE1MDc3MDU4NzgsInN1YiI6IntcInRva2VuXCI6XCJjYTQ5MjlkZGJlYTY4Y2I4OWYwYTE0YzVjYWE4YTk5OFwiLFwidXNlcklkXCI6XCJiM2Q0ZWUwNTRiODE5NWU4Y2UyZGJlY2VkZWZiZmI0OVwifSIsImV4cCI6MTUwNzcwOTQ3OH0.R09HSDbxZgM6zoW0hDHhKDVP9nmKqilLpv8SHAZoS58"
    },
    "status": 200,
    "timestamp": 1507705878257
}
```

2. 得到上一步骤的结果,再次发起GET请求:

   http://localhost:8761/api/service-1/user-info

需要带上请求头: jwt-token:上一步返回json中的token。
得到返回结果类似:
```json
    {
        "attributes": {},
        "permissions": [],
        "roles": [],
        "user": {
            "id": "b3d4ee054b8195e8ce2dbecedefbfb49",
            "name": "super user",
            "username": "admin"
        }
    }
```

测试成功