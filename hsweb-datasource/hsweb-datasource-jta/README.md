# 动态数据源JTA实现 (atomikos)
使用atomikos实现动态数据源事务管理

# 数据源配置
默认数据源配置,使用spring的jta配置即可:
```yaml
spring:
  jta:
    status: true
    atomikos:
      datasource:
        xa-data-source-class-name: com.alibaba.druid.pool.xa.DruidXADataSource
        xa-properties:
          url : jdbc:h2:mem:core;DB_CLOSE_ON_EXIT=FALSE
          username : sa
          password :
        max-pool-size: 20
        borrow-connection-timeout: 1000
      connectionfactory:
        max-pool-size: 20
        local-transaction-mode: true
```

动态数据源配置,默认提供一个 ``InMemoryAtomikosDataSourceRepository``,在application.yml 中进行配置即可:
```yaml
hsweb:
  datasource:
    test_ds:  # 数据源ID
        xa-data-source-class-name: com.alibaba.druid.pool.xa.DruidXADataSource
        xa-properties: # 数据源的配置属性
          url: jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE
          username: sa
          password:
        max-pool-size: 20
        borrow-connection-timeout: 1000
    test_ds2: # 数据源ID
      xa-data-source-class-name: com.alibaba.druid.pool.xa.DruidXADataSource
      xa-properties: # 数据源的配置属性
        url: jdbc:mysql://localhost:3306/hsweb?pinGlobalTxToPhysicalConnection=true&useSSL=false&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false
#            url: jdbc:h2:mem:test2;DB_CLOSE_ON_EXIT=FALSE
        username: root
        password: "123456" # 纯数字密码要加上双引号，不然启动会报Cannot initialize AtomikosDataSourceBean
      max-pool-size: 20
      borrow-connection-timeout: 1000
      init-timeout: 20
```

自定义,将数据源配置放到数据库中,实现 ``DynamicDataSourceConfigRepository<AtomikosDataSourceConfig>`` 接口并注入到spring容器即可
