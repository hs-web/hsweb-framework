## 后台管理基础框架

[![Build Status](https://travis-ci.org/hs-web/hsweb-framework.svg?branch=master)](https://travis-ci.org/hs-web/hsweb-framework)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

### 主要功能
1. 通用dao,service,controller类，增删改查直接继承即可.
2. 通用mybatis配置文件,支持多种条件查询自动生成,支持自动生成insert,update,delete语句,支持和查询相同的各种条件.
3. 实现用户,权限管理;基于aop,注解,精确到按钮的权限控制.
4. 动态表单功能,可在前端设计表单,动态生成数据库表,提供统一的增删改查接口.
5. 在线代码生成器,可自定义模板.
6. 动态多数据源,支持数据源热加载,热切换,支持分布式事务.
7. 数据库支持 mysql,oracle,h2.
8. websocket支持.
9. 定时调度支持,可在页面配置定时任务,编写任务脚本执行。

### 其他组件
1. [hsweb-easy-orm](https://github.com/hs-web/hsweb-easy-orm) :为动态表单设计的orm框架
2. [hsweb-expands-compress](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-compress) :文件压缩，解压操作
3. [hsweb-expands-office](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-office) :office文档操作( excel读写，模板导出，word模板导出)
4. [hsweb-expands-request](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-request): 请求模拟(http,ftp)
5. [hsweb-expands-script](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-script):动态脚本,动态编译执行java,groovy,javascript,spel,ognl....
6. [hsweb-expands-shell](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-shell):shell执行
7. [hsweb-expands-template](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-template):各种模板引擎

# 安装
```bash
    $ git clone https://github.com/hs-web/hsweb-framework.git
    $ cd hsweb-framework
    $ mvn install -DskiptTests
```

# 此版本待完善功能
1. 单元测试编写
2. 项目文档编写
3. 增加定时调度,支持集群,任务采用脚本方式编写.
4. 完善数据库持续集成,版本更新时自动更新数据库结构.
5. 完善动态表单发布,表单发生变化后,自动重新发布(解决集群下,表单配置不一致).

# 演示
1. 示例:[demo.hsweb.me](http://demo.hsweb.me)
2. 测试用户:test (test2,test3,test4....) 密码:123456 
3. 演示项目源码:[hsweb-platform](https://github.com/hs-web/hsweb-platform)