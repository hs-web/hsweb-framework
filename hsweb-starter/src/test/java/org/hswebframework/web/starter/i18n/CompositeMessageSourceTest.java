package org.hswebframework.web.starter.i18n;

import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.junit.Assert.*;

public class CompositeMessageSourceTest {

    @Test
    public void testFormatMessageWithArgs() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        StaticMessageSource staticMessageSource = new StaticMessageSource();
        staticMessageSource.addMessage("message.test", Locale.ENGLISH, "Hello {0}, your code is {1}");
        staticMessageSource.addMessage("message.test", Locale.CHINESE, "你好 {0}, 你的代码是 {1}");

        compositeMessageSource.addMessageSource(staticMessageSource);

        // 测试英文带参数
        String result = compositeMessageSource.getMessage(
            "message.test",
            new Object[]{"World", "12345"},
            null,
            Locale.ENGLISH
        );
        assertEquals("Hello World, your code is 12345", result);

        // 测试中文带参数
        result = compositeMessageSource.getMessage(
            "message.test",
            new Object[]{"张三", "67890"},
            null,
            Locale.CHINESE
        );
        assertEquals("你好 张三, 你的代码是 67890", result);
    }

    @Test
    public void testFormatDefaultMessageWithArgs() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        // 没有添加任何消息源，应该返回格式化后的 defaultMessage
        String result = compositeMessageSource.getMessage(
            "message.not.found",
            new Object[]{"device-001", "connection timeout"},
            "Send failed, device: {0}, error: {1}",
            Locale.ENGLISH
        );
        assertEquals("Send failed, device: device-001, error: connection timeout", result);
    }

    @Test
    public void testFormatMessageWithoutArgs() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        StaticMessageSource staticMessageSource = new StaticMessageSource();
        staticMessageSource.addMessage("message.simple", Locale.ENGLISH, "Simple message");

        compositeMessageSource.addMessageSource(staticMessageSource);

        String result = compositeMessageSource.getMessage(
            "message.simple",
            null,
            null,
            Locale.ENGLISH
        );
        assertEquals("Simple message", result);
    }

    @Test
    public void testFormatMessageWithEmptyArgs() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        StaticMessageSource staticMessageSource = new StaticMessageSource();
        staticMessageSource.addMessage("message.simple", Locale.ENGLISH, "Simple message");

        compositeMessageSource.addMessageSource(staticMessageSource);

        String result = compositeMessageSource.getMessage(
            "message.simple",
            new Object[]{},
            null,
            Locale.ENGLISH
        );
        assertEquals("Simple message", result);
    }

    @Test
    public void testGetMessageWithLocaleOnly() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        StaticMessageSource staticMessageSource = new StaticMessageSource();
        staticMessageSource.addMessage("message.test", Locale.ENGLISH, "Hello {0}");

        compositeMessageSource.addMessageSource(staticMessageSource);

        String result = compositeMessageSource.getMessage(
            "message.test",
            new Object[]{"World"},
            Locale.ENGLISH
        );
        assertEquals("Hello World", result);
    }

    @Test(expected = NoSuchMessageException.class)
    public void testGetMessageThrowsException() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        compositeMessageSource.getMessage(
            "message.not.found",
            new Object[]{"arg1"},
            Locale.ENGLISH
        );
    }

    @Test
    public void testGetMessageWithResolvable() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        StaticMessageSource staticMessageSource = new StaticMessageSource();
        staticMessageSource.addMessage("message.resolvable", Locale.ENGLISH, "Resolvable: {0}");

        compositeMessageSource.addMessageSource(staticMessageSource);

        MessageSourceResolvable resolvable = new MessageSourceResolvable() {
            @Override
            public String[] getCodes() {
                return new String[]{"message.resolvable"};
            }

            @Override
            public Object[] getArguments() {
                return new Object[]{"test-arg"};
            }

            @Override
            public String getDefaultMessage() {
                return "default";
            }
        };

        String result = compositeMessageSource.getMessage(resolvable, Locale.ENGLISH);
        assertEquals("Resolvable: test-arg", result);
    }

    @Test
    public void testMultipleMessageSources() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        StaticMessageSource source1 = new StaticMessageSource();
        source1.addMessage("message.first", Locale.ENGLISH, "First: {0}");

        StaticMessageSource source2 = new StaticMessageSource();
        source2.addMessage("message.second", Locale.ENGLISH, "Second: {0}");

        compositeMessageSource.addMessageSource(source1);
        compositeMessageSource.addMessageSource(source2);

        // 测试第一个消息源
        String result1 = compositeMessageSource.getMessage(
            "message.first",
            new Object[]{"value1"},
            null,
            Locale.ENGLISH
        );
        assertEquals("First: value1", result1);

        // 测试第二个消息源
        String result2 = compositeMessageSource.getMessage(
            "message.second",
            new Object[]{"value2"},
            null,
            Locale.ENGLISH
        );
        assertEquals("Second: value2", result2);
    }

    @Test
    public void testModbusScenario() {
        // 模拟实际的 Modbus 场景
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        StaticMessageSource staticMessageSource = new StaticMessageSource();
        staticMessageSource.addMessage(
            "message.modbus.send_failed",
            Locale.ENGLISH,
            "Modbus message send failed, device: {0}, error: {1}"
        );

        compositeMessageSource.addMessageSource(staticMessageSource);

        String result = compositeMessageSource.getMessage(
            "message.modbus.send_failed",
            new Object[]{"device-123", "Connection refused"},
            "Modbus 消息发送失败, 设备: {0}, 错误: {1}",
            Locale.ENGLISH
        );

        assertEquals("Modbus message send failed, device: device-123, error: Connection refused", result);
    }

    @Test
    public void testNullDefaultMessage() {
        CompositeMessageSource compositeMessageSource = new CompositeMessageSource();

        String result = compositeMessageSource.getMessage(
            "message.not.found",
            new Object[]{"arg1"},
            null,
            Locale.ENGLISH
        );

        assertNull(result);
    }
}
