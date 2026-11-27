package org.hswebframework.web.bean;

import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.exception.RemoteExceptionMetadata;
import org.hswebframework.web.exception.ValidationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FastBeanCopierExceptionEdgeTest {

    @Test
    public void nullIgnoreSetMatchesNullVarargsForAllCopyDirections() {
        Throwable error = new IllegalStateException("failed");
        Map<String, Object> expected = FastBeanCopier.copy(error, new LinkedHashMap<>());
        Assert.assertEquals(expected, FastBeanCopier.copy(error, new LinkedHashMap<>(), (Set<String>) null));
        Assert.assertEquals(expected, FastBeanCopier.copy(error, new LinkedHashMap<>(), (String[]) null));
        FastBeanCopierReadOnceTest.NameTarget source = new FastBeanCopierReadOnceTest.NameTarget();
        source.setName("one");
        Assert.assertEquals(Map.of("name", "one"), FastBeanCopier.copy(source, new LinkedHashMap<>(), (Set<String>) null));
        Assert.assertEquals("one", FastBeanCopier.copy(Map.of("name", "one"),
            new FastBeanCopierReadOnceTest.NameTarget(), (Set<String>) null).getName());
        Assert.assertEquals("failed", FastBeanCopier.copy(expected, new Throwable(), (Set<String>) null).getMessage());
    }

    @Test
    public void cyclicCauseSuppressedAndSharedNodesRemainFinite() {
        Throwable first = new IllegalStateException("first");
        Throwable second = new IllegalArgumentException("second");
        Throwable shared = new IllegalStateException("shared");
        first.initCause(second);
        second.initCause(first);
        first.addSuppressed(shared);
        second.addSuppressed(shared);
        shared.addSuppressed(first);
        for (Throwable value : List.of(first, second, shared)) {
            value.setStackTrace(new StackTraceElement[0]);
        }

        Map<String, Object> encoded = FastBeanCopier.copy(first, new LinkedHashMap<>());
        String detail = (String) encoded.get("detail");
        Assert.assertTrue(detail.contains("CIRCULAR REFERENCE"));
        Assert.assertTrue(detail.contains("Suppressed:"));
        Assert.assertTrue(detail.contains("Caused by:"));
        Assert.assertTrue(detail.length() < 4096);
        Throwable restored = FastBeanCopier.copy(encoded, Throwable.class);
        Assert.assertNull(restored.getCause());
        Assert.assertEquals(0, restored.getSuppressed().length);
        Assert.assertEquals(detail, FastBeanCopier.copy(restored, new LinkedHashMap<>()).get("detail"));
    }

    @Test
    public void excludedCallbacksNeverExecuteAndErrorsAreNotSwallowed() {
        BrokenException source = new BrokenException();
        Assert.assertEquals(Map.of("type", source.getClass().getName()),
                            FastBeanCopier.copy(source, new LinkedHashMap<>(), FastBeanCopier.include("type")));
        Assert.assertEquals(0, source.messageReads);
        Assert.assertEquals(0, source.detailReads);
        Assert.assertSame(source.messageFailure, Assert.assertThrows(AssertionError.class,
            () -> FastBeanCopier.copy(source, new LinkedHashMap<>(), "detail")));
        Assert.assertSame(source.detailFailure, Assert.assertThrows(AssertionError.class,
            () -> FastBeanCopier.copy(source, new LinkedHashMap<>(), "message")));
    }

    @Test
    public void immutableInputsAndMutableDetailsDoNotChangeTheSource() {
        ValidationException.Detail detail = new ValidationException.Detail("field", "invalid", Map.of("value", 12));
        List<Object> args = Collections.unmodifiableList(Arrays.asList(null, new BigDecimal("12.30")));
        Map<String, Object> source = Map.of("type", ValidationException.class.getName(), "message", "failed",
            "args", args, "details", List.of(detail));
        ValidationException restored = FastBeanCopier.copy(source, ValidationException.class);

        Assert.assertArrayEquals(args.toArray(), restored.getArgs());
        restored.getArgs()[0] = "changed";
        restored.getDetails().get(0).setProperty("changed");
        Assert.assertNull(args.get(0));
        Assert.assertEquals("field", detail.getProperty());
        Assert.assertSame(detail.getDetail(), restored.getDetails().get(0).getDetail());
    }

    @Test
    public void statusHandlesExactBoundsAndRejectsOverflowAndNonFiniteValues() {
        for (Number value : List.of(Integer.MIN_VALUE, Integer.MAX_VALUE, BigInteger.valueOf(409),
                new BigDecimal("409.000"), new AtomicInteger(409))) {
            BusinessException restored = FastBeanCopier.copy(Map.of("type", "remote.Status", "status", value), BusinessException.class);
            Assert.assertEquals(value.intValue(), restored.getStatus());
        }
        for (Number value : List.of(new BigInteger("-2147483649"), new BigInteger("2147483648"),
                new BigDecimal("409.0001"), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            Assert.assertThrows(IllegalArgumentException.class,
                () -> FastBeanCopier.copy(Map.of("type", "remote.Status", "status", value), Throwable.class));
        }
    }

    @Test
    public void unicodeLargeDiagnosticsAndRepeatedFailuresDoNotCorruptLaterConversions() {
        String message = "设备\n不可用🙂\t'quoted'";
        String diagnostic = "远端诊断🙂\n".repeat(4096);
        Map<String, Object> source = Map.of("type", "remote.Unicode", "message", message, "detail", diagnostic);
        for (int i = 0; i < 50; i++) {
            Assert.assertThrows(IllegalArgumentException.class,
                () -> FastBeanCopier.copy(Map.of("type", 1), Throwable.class));
            Throwable restored = FastBeanCopier.copy(source, Throwable.class);
            Assert.assertEquals(message, restored.getMessage());
            Assert.assertEquals(diagnostic, ((RemoteExceptionMetadata) restored).getRemoteDetail());
        }
        Assert.assertEquals(source, FastBeanCopier.copy(FastBeanCopier.copy(source, Throwable.class), new LinkedHashMap<>()));
    }

    @Test
    public void cyclicArgumentPayloadsRemainShallowWithoutRecursiveTraversal() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("self", payload);
        Throwable restored = FastBeanCopier.copy(Map.of("type", "remote.Payload", "args", List.of(payload)), Throwable.class);
        Map<String, Object> forwarded = FastBeanCopier.copy(restored, new LinkedHashMap<>());
        Assert.assertSame(payload, ((Object[]) forwarded.get("args"))[0]);
    }

    @Test
    public void restoredValidationWithOrdinaryPayloadRemainsJavaSerializable() throws Exception {
        ValidationException original = new ValidationException("reason",
            List.of(new ValidationException.Detail("field", "invalid", "extra")), 12);
        ValidationException restored = FastBeanCopier.copy(FastBeanCopier.copy(original, new LinkedHashMap<>()), ValidationException.class);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(restored);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            ValidationException decoded = (ValidationException) input.readObject();
            Assert.assertEquals("field", decoded.getDetails().get(0).getProperty());
            Assert.assertEquals("extra", decoded.getDetails().get(0).getDetail());
            Assert.assertEquals(original.getClass().getName(), ((RemoteExceptionMetadata) decoded).getRemoteType());
        }
    }

    public static class BrokenException extends RuntimeException {
        final AssertionError messageFailure = new AssertionError("message failed");
        final AssertionError detailFailure = new AssertionError("detail failed");
        int messageReads;
        int detailReads;

        @Override
        public String getMessage() {
            messageReads++;
            throw messageFailure;
        }

        @Override
        public void printStackTrace(PrintWriter writer) {
            detailReads++;
            throw detailFailure;
        }
    }
}
