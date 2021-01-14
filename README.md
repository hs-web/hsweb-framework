# hsweb4 基于spring-boot2,全响应式的后台管理框架
[![Codecov](https://codecov.io/gh/hs-web/hsweb-framework/branch/4.0.x/graph/badge.svg)](https://codecov.io/gh/hs-web/hsweb-framework/branch/master)
[![Build Status](https://travis-ci.org/hs-web/hsweb-framework.svg?branch=4.0.x)](https://travis-ci.org/hs-web/hsweb-framework)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

# 功能,特性
- [x] 基于[r2dbc](https://github.com/r2dbc) ,[easy-orm](https://github.com/hs-web/hsweb-easy-orm/tree/4.0.x)的通用响应式CRUD
    - [x] H2,Mysql,SqlServer,PostgreSQL
- [x] 响应式r2dbc事务控制
- [x] 响应式权限控制,以及权限信息获取
    - [x] RBAC权限控制
    - [x] 数据权限控制
    - [ ] 双因子验证
- [x] 多维度权限管理功能
- [x] 响应式缓存 
- [ ] 非响应式支持(mvc,jdbc)
- [ ] 内置业务功能
    - [x] 权限管理
        - [x] 用户管理
        - [x] 权限设置
        - [x] 权限分配
    - [ ] 文件上传
        - [x] 静态文件上传
        - [ ] 文件秒传
    - [x] 数据字典

# 文档

TODO

# 实践

[JetLinks开源物联网平台](https://github.com/jetlinks)