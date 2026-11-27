package org.hswebframework.web.i18n;

import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.exception.RemoteException;
import org.hswebframework.web.exception.RemoteExceptionMetadata;
import org.hswebframework.web.exception.I18nSupportException;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.exception.ValidationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RemoteExceptionI18nTest {
    private MessageSource previous;

    @Before
    public void installMessages() {
        previous = LocaleUtils.messageSource;
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("test.restore.reason", Locale.ENGLISH, "Rejected {0,number,integer}");
        messages.addMessage("test.restore.reason", Locale.CHINESE, "拒绝 {0,number,integer}");
        LocaleUtils.messageSource = messages;
    }

    @After
    public void restoreMessages() {
        LocaleUtils.messageSource = previous;
    }

    @Test
    public void restoredReasonRetranslatesNumericArgumentsAndSurvivesHops() {
        Map<String, Object> source = Map.of("type", "remote.BusinessProblem", "message", "Sender fallback",
            "detail", "remote stack", "i18nCode", "test.restore.reason", "args", List.of(12),
            "code", "BUSINESS_REJECTED", "status", 409);

        for (Locale locale : List.of(Locale.CHINESE, Locale.ENGLISH)) {
            LocaleUtils.doWith(locale, ignored -> {
                Map<String, Object> encoded = source;
                for (int i = 0; i < 3; i++) {
                    BusinessException restored = FastBeanCopier.copy(encoded, BusinessException.class);
                    Assert.assertEquals(locale.equals(Locale.CHINESE) ? "拒绝 12" : "Rejected 12", restored.getMessage());
                    Assert.assertEquals(encoded.get("message"), restored.getOriginalMessage());
                    Assert.assertEquals("test.restore.reason", restored.getI18nCode());
                    Assert.assertArrayEquals(new Object[]{12}, restored.getArgs());
                    Assert.assertEquals("BUSINESS_REJECTED", restored.getCode());
                    Assert.assertEquals(409, restored.getStatus());
                    encoded = FastBeanCopier.copy(restored, new LinkedHashMap<>());
                    Assert.assertEquals("remote.BusinessProblem", encoded.get("type"));
                    Assert.assertEquals("remote stack", encoded.get("detail"));
                }
            });
        }
    }

    @Test
    public void untranslatedReasonsKeepSenderMessageAcrossHops() {
        Map<String, Object> source = Map.of("type", "remote.Unknown", "message", "Sender text",
            "i18nCode", "unknown.remote.reason", "args", List.of(12));

        LocaleUtils.doWith(Locale.ENGLISH, ignored -> {
            Map<String, Object> encoded = source;
            for (int i = 0; i < 3; i++) {
                RemoteException restored = (RemoteException) FastBeanCopier.copy(encoded, Throwable.class);
                Assert.assertEquals("Sender text", restored.getMessage());
                Assert.assertEquals("unknown.remote.reason", restored.getI18nCode());
                encoded = FastBeanCopier.copy(restored, new LinkedHashMap<>());
                Assert.assertEquals("remote.Unknown", encoded.get("type"));
                Assert.assertFalse(encoded.containsKey("status"));
                Assert.assertFalse(encoded.containsKey("detail"));
            }
        });
    }

    @Test
    public void nullReasonAndNullMessageRemainAbsent() {
        Throwable restored = FastBeanCopier.copy(Map.of("type", "remote.Unknown"), Throwable.class);

        Assert.assertNull(restored.getMessage());
        Assert.assertEquals(Map.of("type", "remote.Unknown"), FastBeanCopier.copy(restored, new LinkedHashMap<>()));
    }

    @Test
    public void knownPlatformNoStackTraceTypesRetainTranslationAndSenderFallback() {
        for (Class<? extends I18nSupportException> type : List.of(I18nSupportException.class,
                BusinessException.class, NotFoundException.class, ValidationException.class)) {
            LocaleUtils.doWith(Locale.CHINESE, ignored -> {
                Map<String, Object> encoded = Map.of("type", type.getName(), "message", "Sender text",
                    "detail", "remote stack", "i18nCode", "test.restore.reason", "args", List.of(12));
                for (int i = 0; i < 3; i++) {
                    I18nSupportException restored = FastBeanCopier.copy(encoded, type);
                    Assert.assertEquals("拒绝 12", restored.getMessage());
                    Assert.assertEquals(encoded.get("message"), restored.getOriginalMessage());
                    Assert.assertEquals(0, restored.getStackTrace().length);
                    encoded = FastBeanCopier.copy(restored, new LinkedHashMap<>());
                    Assert.assertEquals(type.getName(), ((RemoteExceptionMetadata) restored).getRemoteType());
                    Assert.assertEquals("remote stack", encoded.get("detail"));
                }
                I18nSupportException unknown = FastBeanCopier.copy(Map.of("type", type.getName(),
                    "message", "Sender text", "i18nCode", "unknown.remote.reason"), type);
                Assert.assertEquals("Sender text", unknown.getMessage());
            });
        }
    }
}
