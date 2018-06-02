## hsweb  3.0
[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/nexus.hsweb.me/content/groups/public/org/hswebframework/web/hsweb-framework/maven-metadata.xml.svg)](http://nexus.hsweb.me/#nexus-search;quick~hsweb-framework)
[![Codecov](https://codecov.io/gh/hs-web/hsweb-framework/branch/master/graph/badge.svg)](https://codecov.io/gh/hs-web/hsweb-framework/branch/master)
[![Sonar Coverage](https://sonarcloud.io/api/badges/measure?key=org.hswebframework.web:hsweb-framework&metric=coverage)](https://sonarcloud.io/dashboard?id=org.hswebframework.web%3Ahsweb-framework)
[![Sonar Bugs](https://sonarcloud.io/api/badges/measure?key=org.hswebframework.web:hsweb-framework&metric=bugs)](https://sonarcloud.io/dashboard?id=org.hswebframework.web%3Ahsweb-framework)
[![Build Status](https://travis-ci.org/hs-web/hsweb-framework.svg?branch=master)](https://travis-ci.org/hs-web/hsweb-framework)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Insight.io](https://www.insight.io/repoBadge/github.com/hs-web/hsweb-framework)](https://insight.io/github.com/hs-web/hsweb-framework)

 [贡献代码](CONTRIBUTING.md)  [开发手册](https://github.com/hs-web/hsweb-framework/wiki/开发手册)

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
