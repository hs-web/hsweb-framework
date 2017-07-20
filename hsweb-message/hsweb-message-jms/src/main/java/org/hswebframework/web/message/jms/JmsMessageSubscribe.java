package org.hswebframework.web.message.jms;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessageSubscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class JmsMessageSubscribe<M extends Message> implements MessageSubscribe<M> {
    private List<Consumer<M>> consumers = new ArrayList<>();
    private JmsTemplate jmsTemplate;

    private Executor executor;

    private String subjectName;

    private volatile boolean running = false;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public JmsMessageSubscribe(JmsTemplate jmsTemplate, Executor executor, String subject) {
        this.jmsTemplate = jmsTemplate;
        this.executor = executor;
        this.subjectName = subject;
    }

    @Override
    public MessageSubscribe<M> onMessage(Consumer<M> consumer) {
        consumers.add(consumer);
        if (!running) {
            run();
        }
        return this;
    }

    private void run() {
        running = true;
        executor.execute(() -> {
            while (running) {
                try {
                    M message = (M) jmsTemplate.receiveAndConvert(subjectName);
                    consumers.forEach(con -> con.accept(message));
                } catch (UncategorizedJmsException e) {
                    logger.error("stop subscribe", e);
                    running = false;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    // running = false;
                }
            }
        });
    }

    @Override
    public void cancel() {
        running = false;
    }
}
