# hsweb-framework 
后台管理基础框架,基于maven,spring-boot,mybatis

### 主要功能
1. 基础dao,service,controller类，增删改查直接继承即可.
2. 通用mybatis配置文件,支持多种条件查询自动生成,支持自动生成insert和update语句.
3. 实现用户,权限管理;基于aop,注解,精确到按钮的权限控制.
4. 动态表单功能,可在前端设计表单,动态生成数据库表,提供统一的增删改查接口.
5. 数据库支持 mysql,oracle
# maven安装
```bash
    $ git clone https://github.com/hs-web/hsweb-framework.git
    $ cd hsweb-framework
    $ mvn install
```

# 使用
参照:[hsweb-platform](https://github.com/hs-web/hsweb-platform)