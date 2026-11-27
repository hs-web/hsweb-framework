package org.hswebframework.web.validator;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.i18n.LocaleUtils;
import org.hswebframework.web.i18n.MessageSourceInitializer;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.OverridingClassLoader;

import jakarta.validation.constraints.NotBlank;

import java.util.Locale;
import java.util.LinkedHashMap;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class ValidatorUtilsTest {

    @Test
    public void test() throws Throwable {
        String key = "i18n.validation.property.enabled";
        String previous = System.getProperty(key);
        Thread thread = Thread.currentThread();
        ClassLoader context = thread.getContextClassLoader();
        // 开关在类初始化时读取，隔离启动配置及消息源，避免依赖其他测试的加载顺序。
        ClassLoader isolated = new OverridingClassLoader(getClass().getClassLoader()) {
            @Override
            protected boolean isEligibleForOverriding(String name) {
                return name.startsWith("org.hswebframework.web.");
            }
        };
        try {
            System.setProperty(key, "true");
            thread.setContextClassLoader(isolated);
            Class<?> type = isolated.loadClass(getClass().getName());
            type.getMethod("verifyLocalizedProperties").invoke(type.getConstructor().newInstance());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } finally {
            thread.setContextClassLoader(context);
            if (previous == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, previous);
            }
        }
    }

    public void verifyLocalizedProperties() {
        StaticMessageSource source = new StaticMessageSource();
        source.addMessage("validation.property_validate_failed", Locale.CHINA, "{0} {1}");
        source.addMessage("validation.property_validate_failed", Locale.ENGLISH, "{0} {1}");

        source.addMessage(TestEntity.class.getName() + ".notBlank", Locale.ENGLISH, "Test");
        source.addMessage(TestEntity.class.getName() + ".notBlank", Locale.CHINA, "测试");

        MessageSourceInitializer.init(source);
        test(Locale.CHINA, "不能为空", "测试 不能为空");
        test(Locale.ENGLISH, "must not be blank", "Test must not be blank");
    }

    public void test(Locale locale, String msg, String msg2) {
        try {
            LocaleUtils.doWith(locale, en -> {
                ValidatorUtils.tryValidate(new TestEntity());
            });
            throw new IllegalStateException();
        } catch (ValidationException e) {
            assertEquals(msg, e.getDetails().get(0).getMessage());
            assertEquals(msg2, e.getLocalizedMessage(locale));
            ValidationException restored = FastBeanCopier.copy(FastBeanCopier.copy(e, new LinkedHashMap<>()), ValidationException.class);
            assertEquals(msg2, restored.getLocalizedMessage(locale));
            assertArrayEquals(e.getArgs(), restored.getArgs());
        }
    }

    @Getter
    @Setter
    public static class TestEntity {

        @NotBlank
        private String notBlank;
    }
}