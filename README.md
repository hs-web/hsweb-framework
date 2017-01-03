## hsweb后台管理基础框架

[![Build Status](https://travis-ci.org/hs-web/hsweb-framework.svg?branch=master)](https://travis-ci.org/hs-web/hsweb-framework)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

### 3.0
[全新的架构,开发中...](https://github.com/hs-web/hsweb-framework/tree/3.0)

### 业务功能
现在:

1. 权限管理: 权限资源-角色-用户.
2. 配置管理: kv结构,自定义配置.可通过此功能配置数据字典.
3. 脚本管理: 动态脚本,支持javascript,groovy,java动态编译执行.
4. 表单管理: 动态表单,可视化设计表单,自动生成数据库以及系统权限.无需重启直接生效.
5. 模块设置: 配合动态表单实现表格页,查询条件自定义.
6. 数据库维护: 在线维护数据库,修改表结构,执行sql.
7. 数据源管理: 配置多数据源.
8. 代码生成器: 在线生成代码,打包下载.可自定义模板.
9. 定时任务: 配置定时任务,使用动态脚本编写任务内容.
10. 系统监控: 监控系统资源使用情况.
11. 缓存监控: 监控缓存情况.
12. 访问日志: 记录用户每次操作情况

未来

1. 组织架构管理: 地区-机构-部门-职务-人员.
2. 工作流管理: activiti工作流,在线配置流程,配合动态表单实现自定义流程.
3. 邮件代收: 代收指定邮箱的邮件


### 框架功能
0. 全局restful+json,前后分离.
1. 通用dao,service,controller类，增删改查直接继承即可.
2. 通用mybatis配置文件,支持多种条件查询自动生成,支持自动生成insert,update,delete语句,支持和查询相同的各种条件.
3. 实现用户,权限管理;基于aop,注解,精确到按钮的权限控制.
4. 动态表单功能,可在前端设计表单,动态生成数据库表,提供统一的增删改查接口.
5. 在线代码生成器,可自定义模板.
6. 动态多数据源,支持数据源热加载,热切换,支持分布式事务.
7. 数据库支持 mysql,oracle,h2.
8. websocket支持.
9. 定时调度支持,可在页面配置定时任务,编写任务脚本执行。
10. **强大的dsl查询方式,复杂条件一句生成**

### 演示
1. 示例:[demo.hsweb.me](http://demo.hsweb.me)
2. 测试用户:test (test2,test3,test4....) 密码:123456 
3. 演示项目源码:[hsweb-platform](https://github.com/hs-web/hsweb-platform)

### 文档
1. [安装使用](doc/1.安装使用.md)
2. [API](doc/2.API.md)

### 此版本待完善功能
1. 单元测试编写
2. 项目文档编写
3. ~~增加定时调度,支持集群,任务采用脚本方式编写.~~
4. 完善数据库持续集成,版本更新时自动更新数据库结构.
5. 完善动态表单发布,表单发生变化后,自动重新发布(解决集群下,表单配置不一致).

### 技术选型
第三方:

1. MVC:[spring-boot](https://github.com/spring-projects/spring-boot). 开箱即用,学习成本低,部署方便(main方法运行).
2. ORM:[mybatis](https://github.com/mybatis/mybatis-3). 配置灵活,简单方便.
3. JTA:[atomikos](https://www.atomikos.com/). 分布式事务,多数据源事务全靠他.
4. Cache:[spring-cache](https://github.com/spring-projects/spring-framework/tree/master/spring-context/src/main/java/org/springframework/cache). 统一接口,注解使用,simple,redis... 自动切换.
5. Scheduler:[quartz](https://github.com/quartz-scheduler/quartz). 开源稳定,支持集群.

自家:

0. [hsweb-commons](https://github.com/hs-web/hsweb-commons) :通用工具类
1. [hsweb-easy-orm](https://github.com/hs-web/hsweb-easy-orm) :为动态表单设计的orm框架
2. [hsweb-expands-compress](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-compress) :文件压缩，解压操作
3. [hsweb-expands-office](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-office) :office文档操作( excel读写，模板导出，word模板导出)
4. [hsweb-expands-request](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-request): 请求模拟(http,ftp)
5. [hsweb-expands-script](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-script):动态脚本,动态编译执行java,groovy,javascript,spel,ognl....
6. [hsweb-expands-shell](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-shell):shell执行
7. [hsweb-expands-template](https://github.com/hs-web/hsweb-expands/tree/master/hsweb-expands-template):各种模板引擎
