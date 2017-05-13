## 消息模块，提供简单的消息收发功能

1. [hsweb-message-api](hsweb-message-api) API
2. [hsweb-message-jms](hsweb-message-jms) JMS实现
3. [hsweb-message-memory](hsweb-message-memory) 内存实现
4. [hsweb-message-redis](hsweb-message-redis) Redis实现(redisson)
5. [hsweb-message-websocket](hsweb-message-websocket) 使用websocket进行消息推送


## API
```java
import org.hswebframework.web.message.Messager;
import static org.hswebframework.web.message.builder.StaticMessageBuilder.object;
import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.queue;
import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.topic;

@Autowired
private Messager messager;

public void sendToQueue(){
    messager.publish(text("hello queue!"))
            .to(queue("test_queue"))
            .send();
}

public void subscribeQueue(){
    messager.<TextMessage>subscribe(queue("test_queue"))
            .onMessage(textMsg->System.out.println(textMsg.getMessage()));
    
public void sendToTopic(){
    messager.publish(text("hello topic!"))
            .to(queue("test_topic"))
            .send();
}

public void subscribeTopic(){
    messager.<TextMessage>subscribe(queue("test_topic"))
            .onMessage(textMsg->System.out.println(textMsg.getMessage()));
}
```