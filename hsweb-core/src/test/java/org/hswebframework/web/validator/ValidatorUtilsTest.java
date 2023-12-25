package org.hswebframework.web.validator;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.i18n.LocaleUtils;
import org.hswebframework.web.i18n.MessageSourceInitializer;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;

import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

import static org.junit.Assert.*;

public class ValidatorUtilsTest {

    static {
        System.setProperty("i18n.validation.property.enabled", "true");
    }

    @Test
    public void test() {
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
        }
    }

    @Getter
    @Setter
    public static class TestEntity {

        @NotBlank
        private String notBlank;
    }
}