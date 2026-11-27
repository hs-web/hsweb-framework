package org.hswebframework.web.bean;

import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.exception.I18nSupportException;
import org.hswebframework.web.exception.RemoteException;
import org.hswebframework.web.exception.RemoteExceptionMetadata;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;

public class FastBeanCopierOtherExceptionTest {

    @Test
    public void otherExceptionFamiliesUseSafeDescriptionsAndRetainCompatibleLiveValues() {
        Throwable cause = new IllegalStateException("cause");
        for (Throwable original : Arrays.asList(
            new Throwable("root"), new Exception("checked"), new IOException("io"),
            new NullPointerException("null"), new IllegalArgumentException("argument"),
            new AssertionError("assertion"), new LinkageError("linkage"),
            new CompletionException("completion", cause), new InvocationTargetException(cause, "invocation"),
            new CustomException("custom"), new CustomI18nException("custom.i18n", 12),
            new CustomBusinessException("custom.business", "CUSTOM", 409, 12))) {
            Map<String, Object> encoded = FastBeanCopier.copy(original, new LinkedHashMap<String, Object>());
            Assert.assertEquals(original.getClass().getName(), encoded.get("type"));
            Assert.assertEquals(original.getMessage(), encoded.get("message"));
            Assert.assertTrue(original.getClass().getName(), ((String) encoded.get("detail")).contains(original.toString()));
            for (String key : Arrays.asList("cause", "suppressed", "stackTrace", "localizedMessage", "dangerous")) {
                Assert.assertFalse(encoded.containsKey(key));
            }
            if (original instanceof I18nSupportException) {
                I18nSupportException i18n = (I18nSupportException) original;
                Assert.assertEquals(i18n.getI18nCode(), encoded.get("i18nCode"));
                Assert.assertArrayEquals(i18n.getArgs(), (Object[]) encoded.get("args"));
            }
            if (original instanceof BusinessException) {
                BusinessException business = (BusinessException) original;
                Assert.assertEquals(business.getCode(), encoded.get("code"));
                Assert.assertEquals(business.getStatus(), encoded.get("status"));
            }
            Throwable restored = FastBeanCopier.copy(encoded, Throwable.class);
            Assert.assertTrue(restored instanceof RemoteException);
            Assert.assertEquals(original.getMessage(), restored.getMessage());
            Assert.assertEquals(0, restored.getStackTrace().length);
            Assert.assertEquals(encoded.get("type"), ((RemoteExceptionMetadata) restored).getRemoteType());
            Assert.assertEquals(encoded.get("detail"), ((RemoteExceptionMetadata) restored).getRemoteDetail());
            Map<String, Object> forwarded = FastBeanCopier.copy(restored, new LinkedHashMap<String, Object>());
            Assert.assertEquals(encoded.keySet(), forwarded.keySet());
            encoded.forEach((key, value) -> Assert.assertTrue(key, Objects.deepEquals(value, forwarded.get(key))));
            Assert.assertSame(original, FastBeanCopier.copy(original, Throwable.class));
            Assert.assertSame(original, FastBeanCopier.DEFAULT_CONVERT.convert(original, original.getClass(), null));
            if (!original.getClass().isInstance(restored)) {
                Assert.assertThrows(UnsupportedOperationException.class, () -> FastBeanCopier.copy(encoded, original.getClass()));
            }
        }
    }

    public static class CustomException extends RuntimeException {
        public CustomException(String message) { super(message); }
        public String getDangerous() { throw new AssertionError("arbitrary getter must not run"); }
    }

    public static class CustomI18nException extends I18nSupportException {
        public CustomI18nException(String message, Object... args) { super(message, args); }
    }

    public static class CustomBusinessException extends BusinessException {
        public CustomBusinessException(String message, String code, int status, Object... args) {
            super(message, code, status, args);
        }
    }
}
