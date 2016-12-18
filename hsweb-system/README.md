## 系统功能模块
系统基本功能都在这里,此模块的所有子模块都按照功能来划分.

如:
```text
-----hsweb-system-config
           |------hsweb-system-config-bean
           |------hsweb-system-config-service
                            |----------hsweb-system-config-service-api
                            |----------hsweb-system-config-service-simple
                            |----------hsweb-system-config-service-spring-cloud
                            |----------hsweb-system-config-service-dubbo
           |------hsweb-system-config-dao
                            |------hsweb-system-config-dao-api
                            |------hsweb-system-config-dao-mybatis
                            |------hsweb-system-config-dao-jpa
``` 

1. hsweb-system-all:所有功能依赖整合
2. hsweb-system-authorization:权限管理
3. hsweb-system-cli:命令行功能
4. hsweb-system-config:配置管理
5. hsweb-system-database-manager:数据库管理
6. hsweb-system-datasource:数据源管理
7. hsweb-system-dynamic-form:动态表单
8. hsweb-system-monitor:系统监控
9. hsweb-system-schedule: 定时调度
10. hsweb-system-organizational: 组织架构
11. hsweb-system-workflow: 工作流
