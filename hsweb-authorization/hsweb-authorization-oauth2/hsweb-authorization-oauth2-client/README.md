# OAuth2客户端API
本模块只提供接口,未提供实现,使用时请自行引入相关实现模块

## 处理OAuth2授权码方式的回调
方式一、创建一个类并实现 `OAuth2Listener` 使用`OAuth2CodeAuthBeforeEvent`作为泛型,例如
```java
 public class MyOAuth2Listener
         implements OAuth2Listener<OAuth2CodeAuthBeforeEvent> {
     @Override
     public void on(OAuth2CodeAuthBeforeEvent event) {
          String authCode= event.getCode();
     }
 }
```

注册到对应的oauth2服务配置,例如:
```java
@Autowired
OAuth2RequestService requestService;
public void demo(){
      requestService.registerListener("oauth2_server",new MyOAuth2Listener());
}
```

方式二、使用`AutoRegisterOAuth2Listener`
```java
 @Component
 public class MyOAuth2Listener
         implements AutoRegisterOAuth2Listener<OAuth2CodeAuthBeforeEvent> {
    @Override
    public String getServerId(){
        return "oauth2_server";
    }
    @Override
    public void on(OAuth2CodeAuthBeforeEvent event) {
        String authCode= event.getCode();
    }
 }
```

## 发起OAuth2请求
```java
@Autowired
OAuth2RequestService requestService;

public void demo(){
   //第一步
   OAuth2Session session = requestService
                .create(oatuh2ServerId)
                .byAuthorizationCode(authorizationCode); //使用授权码方式,将自动获取access_token信息并存入会话
  
    //第二步
    String oauth2ApiUri = "oauth2/user-auth-info";
    Authentication authentication = session
                   .request(oauth2ApiUri)       // 创建api请求,将自动使用第一步获得的token
                   .get().ifSuccess()           // http GET请求
                   .as(Authentication.class);   // 响应结果转为Class
}
```