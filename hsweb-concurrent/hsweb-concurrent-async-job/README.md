# 异步任务工具,支持多线程事务
```xml

<dependency>
    <groupId>org.hswebframework.web</groupId>
    <artifactId>hsweb-concurrent-async-job</artifactId>
    <version>${project.verion}</version>
</dependency>
```

```java
   @Autowired
    private AsyncJobService asyncJobService;
    
    
    public void testJob(){
      List<Object> results=  asyncJobService.batch()
                    .submit(()->...) //提交job
                    .submit(()->...) //提交另外一个job
                    .submit(()->...,true) //提交支持事务的job
                    .getResult();
    }

```