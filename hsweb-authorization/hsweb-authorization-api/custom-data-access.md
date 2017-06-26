# 自定义拓展数据权限控制

1. 编写配置转换器,将在前端配置的内容转换为api需要的配置信息

实现 ``DataAccessConfigConvert``接口
```java
@org.springframework.stereotype.Component
public class MyDataAccessConfigConvert implements DataAccessConfigConvert {

    @Override
    public boolean isSupport(String type, String action, String config) {
        return "custom_type".equals(type);
    }

    @Override
    public DataAccessConfig convert(String type, String action, String config) {
        MyDataAccessConfig accessConfig = JSON.parseObject(config, MyDataAccessConfig.class);
        accessConfig.setAction(action);
        accessConfig.setType(type);
        return accessConfig;
    }
}
```


2. 实现 ``DataAccessHandler``接口
```java
@org.springframework.stereotype.Component //提供给Spring才会生效
public class MyDataAccessHandler implements org.hswebframework.web.authorization.access.DataAccessHandler{
    
        @Override
        public boolean isSupport(DataAccessConfig access) {
            //DataAccessConfig 在用户登录的时候,初始化
            //DataAccessConfig 由
            //支持的配置类型
            return "custom_type".equals(access.getType());
        }
    
        //处理请求,返回true表示授权通过
        @Override
        public boolean handle(DataAccessConfig access, MethodInterceptorParamContext context) {
            //被拦截的方法参数
           Map<String,Object> param= context.getParams();
           // 判断逻辑
           //...
            return true;
        }
}
```