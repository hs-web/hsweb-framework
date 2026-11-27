package org.hswebframework.web.bean;

import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.i18n.LocaleUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FastBeanCopierExceptionTest {

    @Test
    public void exceptionAndErrorUseOnlyDescriptionFields() {
        for (Throwable error : new Throwable[]{new IllegalStateException("failed"), new AssertionError("failed")}) {
            error.initCause(new IllegalArgumentException("cause"));
            error.addSuppressed(new IllegalStateException("suppressed"));
            error.setStackTrace(new StackTraceElement[]{new StackTraceElement("example.Device", "read", "Device.java", 42)});

            Map<String, Object> map = FastBeanCopier.copy(error, new LinkedHashMap<>());

            Assert.assertEquals(Set.of("type", "message", "detail"), map.keySet());
            Assert.assertEquals(error.getClass().getName(), map.get("type"));
            Assert.assertEquals("failed", map.get("message"));
            String detail = (String) map.get("detail");
            Assert.assertTrue(detail.contains("example.Device.read(Device.java:42)"));
            Assert.assertTrue(detail.contains("Caused by: java.lang.IllegalArgumentException: cause"));
            Assert.assertTrue(detail.contains("Suppressed: java.lang.IllegalStateException: suppressed"));
        }
    }

    @Test
    public void includeAndIgnoreSkipDetailGenerationAndArbitraryGetters() {
        TrackingException error = new TrackingException("failed");

        Assert.assertEquals(Map.of("type", error.getClass().getName(), "message", "failed"),
                            FastBeanCopier.copy(error, new LinkedHashMap<>(), "detail"));
        Assert.assertEquals(1, error.messageReads);
        Assert.assertEquals(0, error.detailReads);

        Assert.assertEquals(Map.of("type", error.getClass().getName()),
                            FastBeanCopier.copy(error, new LinkedHashMap<>(), FastBeanCopier.include("type")));
        Assert.assertEquals(1, error.messageReads);
        Assert.assertEquals(0, error.detailReads);
    }

    @Test
    public void allPublicMapConversionEntrypointsShareExceptionMapping() {
        Throwable error = new IllegalArgumentException("failed");
        Map<String, Object> expected = FastBeanCopier.copy(error, new LinkedHashMap<>());

        Assert.assertEquals(expected, FastBeanCopier.copy(error, LinkedHashMap::new));
        Assert.assertEquals(expected, FastBeanCopier.copy(error, LinkedHashMap.class));
        Assert.assertEquals(expected, FastBeanCopier.DEFAULT_CONVERT.convert(error, Map.class,
            FastBeanCopier.EMPTY_CLASS_ARRAY));
    }

    @Test
    public void propertyLookupStillReadsOriginalExceptionProperties() {
        Throwable error = new IllegalStateException("failed", new IllegalArgumentException("cause"));

        Assert.assertSame(error.getCause(), FastBeanCopier.getProperty(error, "cause"));
        Assert.assertEquals("failed", FastBeanCopier.getProperty(error, "message"));
    }

    @Test
    public void nullMessageDoesNotOverwriteTarget() {
        Map<String, Object> target = new LinkedHashMap<>(Map.of("message", "retained"));

        FastBeanCopier.copy(new IllegalStateException(), target, "detail");

        Assert.assertEquals("retained", target.get("message"));
        Assert.assertEquals(IllegalStateException.class.getName(), target.get("type"));
    }

    @Test
    public void i18nReasonBusinessCodeAndTypedArgumentsStayDistinct() {
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("test.reason", Locale.ENGLISH, "Rejected {0,number,integer}");
        messages.addMessage("test.reason", Locale.CHINESE, "拒绝 {0,number,integer}");
        BusinessException error = new BusinessException("test.reason", "BUSINESS_REJECTED", 409, 12) {
            @Override
            public String getLocalizedMessage(Locale locale) {
                return messages.getMessage(getI18nCode(), getArgs(), getOriginalMessage(), locale);
            }
        };

        Map<String, Object> map = LocaleUtils.doWith(Locale.ENGLISH,
            () -> FastBeanCopier.copy(error, new LinkedHashMap<>(), "detail"));

        Assert.assertEquals("Rejected 12", map.get("message"));
        Assert.assertEquals("test.reason", map.get("i18nCode"));
        Assert.assertEquals("BUSINESS_REJECTED", map.get("code"));
        Assert.assertEquals(409, map.get("status"));
        Assert.assertArrayEquals(new Object[]{12}, (Object[]) map.get("args"));
        Assert.assertEquals("拒绝 12", messages.getMessage((String) map.get("i18nCode"),
                                                       (Object[]) map.get("args"), Locale.CHINESE));
    }

    @Test
    public void customValueConverterIsAppliedAndNullConversionSkipsWriting() {
        Converter converter = new Converter() {
            @Override
            public <T> T convert(Object source, Class<T> targetClass, Class[] genericType) {
                return source.equals("failed") ? null : FastBeanCopier.DEFAULT_CONVERT.convert(source, targetClass, genericType);
            }
        };
        Map<String, Object> target = new LinkedHashMap<>(Map.of("message", "retained"));

        FastBeanCopier.copy(new IllegalStateException("failed"), target, converter, "detail");

        Assert.assertEquals(Map.of("type", IllegalStateException.class.getName(), "message", "retained"), target);
    }

    public static class TrackingException extends RuntimeException {
        int messageReads;
        int detailReads;

        public TrackingException(String message) {
            super(message);
        }

        @Override
        public String getMessage() {
            messageReads++;
            return super.getMessage();
        }

        @Override
        public void printStackTrace(PrintWriter writer) {
            detailReads++;
            super.printStackTrace(writer);
        }

        public Object getRuntimeResource() {
            throw new AssertionError("Arbitrary exception getter must not be called");
        }
    }
}
