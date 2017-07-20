# websocket支持
此模块使用命令模式,提供统一的socket链接

# socket api
1. ws://localhost:8080/socket  使用html5
2. ws://localhost:8080/sockjs  不支持html5的浏览器使用sockjs

# 开发命令处理器
创建类,实现 `CommandProcessor`接口,例如:

```java
import org.hswebframework.web.socket.processor.CommandProcessor;

@Component  //注意,需要注入spring,命令才会生效,或者使用CommandProcessorContainer.install进行安装
public class MyCommand implements CommandProcessor {
    @Override
    public String getName() {
        return "my-command";
    }

    @Override
    public void execute(CommandRequest request) {
        request.getSession(); // WebSocketSession
        request.getAuthentication(); //获取权限信息
        request.getParameters(); //参数
    }

    @Override
    public void init() {
        //初始化时调用
    }

    @Override
    public void destroy() {
        //销毁时调用
    }
}

```

客户端请求
```js
var ws = new WebSocket("ws://localhost:8080/socket");
//其他设置....
// 将调用上面类的execute方法
 ws.send('{"command":"my-command","parameters":{"type":"conn"}}');
```

更多例子请看: [TestProcessor](src/test/java/org/hswebframework/web/socket/TestProcessor.java) ,
 [WebSocketClientTest](src/test/java/org/hswebframework/web/socket/WebSocketClientTest.java) ,
[WebSocketServerTests](src/test/java/org/hswebframework/web/socket/WebSocketServerTests.java)

