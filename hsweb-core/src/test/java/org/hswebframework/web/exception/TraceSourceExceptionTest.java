package org.hswebframework.web.exception;

import org.hswebframework.web.bean.FastBeanCopier;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TraceSourceExceptionTest {

    @Test
    public void anonymousAndLocalClassesUseBinaryNamesWhenCanonicalNamesAreAbsent() {
        class LocalException extends TraceSourceException {
            LocalException(String message) { super(message); }
        }
        for (TraceSourceException error : new TraceSourceException[]{
            new TraceSourceException() { }, new TraceSourceException("failed") { },
            new LocalException(null), new LocalException("failed")}) {
            assertNull(error.getClass().getCanonicalName());
            String expected = error.getClass().getName();
            if (error.getMessage() != null) {
                expected += ":" + error.getMessage();
            }
            assertEquals(expected, error.toString());
            assertEquals(expected + "\n\t[Operation] ⇢ load\n\t   [Source] ⇢ row",
                         error.withSource("load", "row").toString());
        }
    }

    @Test
    public void nullNameFromAnOverrideAlsoUsesTheBinaryClassName() {
        NullNameException error = new NullNameException();
        assertNotNull(error.getClass().getCanonicalName());
        assertEquals(error.getClass().getName(), error.toString());
    }

    @Test
    public void namedClassFormattingAndNullableSourceTextRemainCompatible() {
        String name = TraceSourceException.class.getCanonicalName();
        assertEquals(name, new TraceSourceException().toString());
        assertEquals(name + ":", new TraceSourceException("").toString());
        assertEquals(name + ":failed\n\t[Operation] ⇢ load\n\t   [Source] ⇢ row",
                     new TraceSourceException("failed").withSource("load", "row").toString());
        assertEquals(name, new TraceSourceException().withSource(new Object() {
            @Override
            public String toString() { return null; }
        }).toString());
    }

    @Test
    public void anonymousPlatformExceptionsCanExportDirectAndNestedDiagnostics() {
        for (TraceSourceException error : new TraceSourceException[]{
            new I18nSupportException("trace.reason") { },
            new BusinessException("trace.reason", "BUSINESS") { },
            new ValidationException("trace.reason") { }}) {
            error.withSource("load", "row");
            Map<String, Object> encoded = FastBeanCopier.copy(error, new LinkedHashMap<>());
            assertEquals(error.getClass().getName(), encoded.get("type"));
            String detail = (String) encoded.get("detail");
            assertTrue(detail.contains(error.toString()));
            assertTrue(detail.contains("[Operation] ⇢ load"));
            assertTrue(detail.contains("[Source] ⇢ row"));
            RuntimeException outer = new RuntimeException("outer", error);
            outer.addSuppressed(error);
            String nested = (String) FastBeanCopier.copy(outer, new LinkedHashMap<>()).get("detail");
            assertTrue(nested.contains(error.toString()));
            assertTrue(nested.contains("Suppressed:"));
            assertTrue(nested.contains("Caused by:"));
        }
    }

    public static class NullNameException extends TraceSourceException {
        @Override
        protected String getExceptionName() { return null; }
    }


    @Test
    public void test() {

        TraceSourceException exp = new TraceSourceException()
            .withSource("test","testSource");


        {
            assertEquals("test", TraceSourceException.tryGetOperation(exp));

            assertEquals("testSource", TraceSourceException.tryGetSource(exp));

        }
        {
            RuntimeException e = new RuntimeException();
            e.addSuppressed(exp);
            assertEquals("test", TraceSourceException.tryGetOperation(e));

            assertEquals("testSource", TraceSourceException.tryGetSource(e));
            e.printStackTrace();
        }

        {
            assertEquals("test", TraceSourceException
                .tryGetOperation(
                    new RuntimeException(exp)
                ));
            assertEquals("testSource", TraceSourceException
                .tryGetSource(
                    new RuntimeException(exp)
                ));

        }

    }
}