## 声明记录访问日志

1. 如果你是maven工程
    * 引入私服配置
    
            <repositories>
                <repository>
                    <id>hsweb-nexus</id>
                    <name>Nexus Release Repository</name>
                    <url>http://nexus.hsweb.me/content/groups/public/</url>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
            </repositories>
    
    * 直接引入依赖

            <dependency>
                <groupId>org.hswebframework.web</groupId>
                <artifactId>hsweb-access-logging-aop</artifactId>
                <version>3.0-SNAPSHOT</version>
            </dependency>
		
	
2. 如果你是非maven工程，[请自行去以下地址](http://nexus.hsweb.me/)下载jar包
		
## 监听访问日志

1. 开启访问日志
    * 在启动类中注解@EnableAccessLogger
    
            @SpringBootApplication
            @EnableAccessLogger
            public class AppApplication {
                public static void main(String[] args) {
                    SpringApplication.run(AppApplication.class, args);
                }	
            }
2. 访问日志 API

    * controller类或者方法上,注解 @AccessLogger("功能描述")
    
            @AccessLogger("hello")
            @RequestMapping(value = "/",method = RequestMethod.GET)
            public String  hello() {
                return "Hello World ! ";
            }        
    
3. 日志监听

    * 创建类,实现: AccessLoggerListener接口并注入到spring容器, 当有日志产生时,会调用接口方法onLogger,并传入日志信息
    
            @Component
            public class MyLoggingListener implements AccessLoggerListener {
                @Override
                public void onLogger(AccessLoggerInfo loggerInfo) {
                    System.out.println(loggerInfo.toString());
                }
            }
    
 
## 日志序列化