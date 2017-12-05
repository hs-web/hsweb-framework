package org.hswebframework.web.service.script.simple;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.entity.script.ScriptEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.message.MessageSubscribe;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.builder.StaticMessageSubjectBuilder;
import org.hswebframework.web.service.script.ScriptExecutorService;
import org.hswebframework.web.service.script.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.*;
import static org.springframework.beans.factory.wiring.BeanWiringInfo.AUTOWIRE_BY_TYPE;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class MessagerScriptExecutorService implements ScriptExecutorService {

    @Autowired
    private Messager messager;

    private String tag;

    private String all = "all";

    private ScriptExecutorService defaultScriptExecutorService;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private void initSubscribe(String topic) {
        messager.subscribe(queue(topic))
                .onMessage(message -> {
                    ScriptExecutorMessage msg = ((ScriptExecutorMessage) message);
                    Object result;
                    try {
                        result = execute(msg.getScriptId(), msg.getParameter());

                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("execute script {} error", msg, e);
                        result = e.getMessage();
                    }
                    messager.publish(ScriptExecutorResultMessage.builder().result(result).build())
                            .to(queue(topic + msg.getCallback()))
                            .send();
                });
    }

    @PostConstruct
    public void subscribe() {
        defaultScriptExecutorService = (DefaultScriptExecutorService) applicationContext.getAutowireCapableBeanFactory()
                .autowire(DefaultScriptExecutorService.class, AUTOWIRE_BY_TYPE, false);

        if (tag != null) {
            String[] tags = tag.split(",");
            for (String tag : tags) {
                initSubscribe(tag);
            }
        } else {
            initSubscribe(all);
        }
    }

    public Object doExecute(String id, Map<String, Object> parameters) throws Exception {
        return defaultScriptExecutorService.execute(id, parameters);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Object execute(String id, Map<String, Object> parameters) throws Exception {
        ScriptEntity scriptEntity = scriptService.selectByPk(id);
        if (scriptEntity == null) {
            return null;
        }
        String configTag = scriptEntity.getTag();
        String callBack = IDGenerator.MD5.generate();

        String topic;

        if (StringUtils.isEmpty(configTag)) {
            topic = all;
        } else {
            String[] tags = configTag.split(",");
            topic = tags[new Random().nextInt(tags.length)];
        }
        Object[] result = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        MessageSubscribe<ScriptExecutorResultMessage> subscribe =
                messager.<ScriptExecutorResultMessage>subscribe(queue(topic + callBack))
                        .onMessage(msg -> {
                            result[0] = msg.getResult();
                            latch.countDown();
                        });

        messager.publish(ScriptExecutorMessage.builder()
                .callback(callBack)
                .parameter(parameters)
                .scriptId(id)
                .build())
                .to(queue(topic)).send();

        boolean success = latch.await(30, TimeUnit.SECONDS);
        if (!success) {
            log.error("await script execute error");
        }
        subscribe.cancel();

        return result[1];
    }


}
