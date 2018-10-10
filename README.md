## hsweb  3.0
[![Maven Central](https://img.shields.io/maven-central/v/org.hswebframework.web/hsweb-framework.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.hswebframework)
[![Codecov](https://codecov.io/gh/hs-web/hsweb-framework/branch/master/graph/badge.svg)](https://codecov.io/gh/hs-web/hsweb-framework/branch/master)
[![Build Status](https://travis-ci.org/hs-web/hsweb-framework.svg?branch=master)](https://travis-ci.org/hs-web/hsweb-framework)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

  [贡献代码](CONTRIBUTING.md)  [快速开始](quick-start)

## 应用场景
1. 后台管理系统.
2. 完全开源的后台管理系统.
3. 细粒度权限控制的后台管理系统.
4. 模块化的后台管理系统.
5. 功能可拓展的后台管理系统.
6. 集成各种常用功能的后台管理系统.
7. 前后分离的后台管理系统.

注意:
项目主要基于`spring-boot`,`mybatis`. 在使用`hsweb`之前,你应该对`spring-boot`有一定的了解.

项目模块太多?不要被吓到.我们不推荐将本项目直接`clone`后修改,运行.而是使用maven依赖的方式使用`hsweb`. 
选择自己需要的模块进行依赖,正式版发布后,所有模块都将发布到maven中央仓库.
你可以参照[demo](https://github.com/hs-web/hsweb3-demo)进行使用.

## 文档
各个模块的使用方式查看对应模块下的 `README.md`,在使用之前,
你可以先粗略浏览一下各个模块,对每个模块的作用有大致的了解.

## 模块简介

| 模块       | 说明          |   进度 |
| ------------- |:-------------:| ----|
|[hsweb-authorization](hsweb-authorization)|权限控制| 90%|
|[hsweb-commons](hsweb-commons) |基础通用功能| 90%|
|[hsweb-concurrent](hsweb-concurrent)|并发包,缓存,锁,计数器等| 80%|
|[hsweb-core](hsweb-core)|框架核心,基础工具类| 90%|
|[hsweb-datasource](hsweb-datasource)|数据源| 90%|
|[hsweb-logging](hsweb-logging)| 日志|  100%|
|[hsweb-message](hsweb-message)|mq,websocket...| 80%|
|[hsweb-starter](hsweb-starter)|模块启动器| 90%|
|[hsweb-system](hsweb-system)|**系统常用功能**| 80%|
|[hsweb-thirdparty](hsweb-thirdparty)| 第三方插件 | 100% |
