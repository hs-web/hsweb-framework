package org.hswebframework.web.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.exception.I18nSupportException;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.exception.RemoteException;
import org.hswebframework.web.exception.RemoteExceptionMetadata;
import org.hswebframework.web.exception.ValidationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FastBeanCopierExceptionRestoreTest {

    @Test
    public void liveExceptionsRetainIdentityInBeansRecordsAndDirectConversion() {
        BusinessException original = new BusinessException("reason", "BUSINESS", 409, 12);
        original.initCause(new IllegalStateException("cause"));
        original.addSuppressed(new IllegalArgumentException("suppressed"));

        ThrowableHolder holder = FastBeanCopier.copy(Map.of("error", original), new ThrowableHolder());
        Assert.assertSame(original, holder.getError());
        Assert.assertSame(original.getCause(), holder.getError().getCause());
        Assert.assertSame(original, FastBeanCopier.copy(Map.of("error", original), new BusinessHolder()).getError());
        Assert.assertSame(original, FastBeanCopier.copy(Map.of("error", original), ErrorRecord.class).error());
        Assert.assertSame(original, FastBeanCopier.copy(original, Throwable.class));
        Assert.assertSame(original, FastBeanCopier.copy(original, new Throwable()));
        Assert.assertSame(original, FastBeanCopier.DEFAULT_CONVERT.convert(original, Throwable.class,
            FastBeanCopier.EMPTY_CLASS_ARRAY));
        Assert.assertSame(original, FastBeanCopier.copy(Map.of("errors", List.of(original)), new ErrorList()).getErrors().get(0));
        Error error = new AssertionError("local error");
        Assert.assertSame(error, FastBeanCopier.copy(Map.of("error", error), new ThrowableHolder()).getError());
    }

    @Test
    public void descriptionRestoresMessageAndRemoteMetadataForAllSupportedTargets() {
        Map<String, Object> description = description();
        for (Class<? extends Throwable> target : List.of(Throwable.class, Exception.class, RuntimeException.class,
                I18nSupportException.class, BusinessException.class, BusinessException.NoStackTrace.class, RemoteException.class)) {
            Throwable restored = FastBeanCopier.copy(description, target);
            Assert.assertTrue(target.isInstance(restored));
            RemoteException remote = (RemoteException) restored;
            Assert.assertEquals("failed", remote.getMessage());
            Assert.assertEquals("remote.Problem", remote.getRemoteType());
            Assert.assertEquals("remote stack", remote.getRemoteDetail());
            Assert.assertEquals(0, remote.getStackTrace().length);
            Assert.assertEquals(description, FastBeanCopier.copy(remote, new LinkedHashMap<>()));
        }
    }

    @Test
    public void platformFamiliesRestoreTheirNoStackTraceImplementations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<ValidationException.Detail> details = List.of(new ValidationException.Detail("field", "invalid", "extra"));
        for (I18nSupportException original : List.of(
                new I18nSupportException("reason", 12), new I18nSupportException.NoStackTrace("reason", 12),
                new BusinessException("reason", "BUSINESS", 409, 12),
                new BusinessException.NoStackTrace("reason", "BUSINESS", 409, 12),
                new NotFoundException("reason", 12), new NotFoundException.NoStackTrace("reason", 12),
                new ValidationException("reason", details, 12), new ValidationException.NoStackTrace("reason", details, 12))) {
            Map<String, Object> encoded = FastBeanCopier.copy(original, new LinkedHashMap<>());
            Map<?, ?> decoded = mapper.readValue(mapper.writeValueAsString(encoded), Map.class);
            I18nSupportException restored = (I18nSupportException) FastBeanCopier.copy(decoded, Throwable.class);
            Class<?> expected = original instanceof NotFoundException ? NotFoundException.NoStackTrace.class
                : original instanceof ValidationException ? ValidationException.NoStackTrace.class
                : original instanceof BusinessException ? BusinessException.NoStackTrace.class : I18nSupportException.NoStackTrace.class;
            Assert.assertTrue(expected.isInstance(restored));
            Assert.assertEquals(0, restored.getStackTrace().length);
            Assert.assertEquals(original.getMessage(), restored.getMessage());
            Assert.assertEquals(restored.getMessage(), FastBeanCopier.getProperty(restored, "message"));
            Assert.assertEquals(original.getI18nCode(), restored.getI18nCode());
            Assert.assertArrayEquals(original.getArgs(), restored.getArgs());
            Assert.assertEquals(original.getClass().getName(), ((RemoteExceptionMetadata) restored).getRemoteType());
            Assert.assertEquals(encoded.get("detail"), ((RemoteExceptionMetadata) restored).getRemoteDetail());
            Assert.assertTrue(original.getClass().isInstance(FastBeanCopier.copy(decoded, original.getClass())));
            if (original instanceof BusinessException business) {
                Assert.assertEquals(business.getStatus(), ((BusinessException) restored).getStatus());
                Assert.assertEquals(business.getCode(), ((BusinessException) restored).getCode());
            }
            if (restored instanceof ValidationException validation) {
                Assert.assertEquals("field", validation.getDetails().get(0).getProperty());
                Assert.assertEquals("invalid", validation.getDetails().get(0).getMessage());
                Assert.assertEquals("extra", validation.getDetails().get(0).getDetail());
                Assert.assertEquals(encoded.get("details"), FastBeanCopier.copy(restored, new LinkedHashMap<>()).get("details"));
            }
        }
    }

    @Test
    public void concretePlatformBeanFieldsRestoreTheirOwnFamilies() {
        Map<String, Object> source = Map.of(
            "missing", FastBeanCopier.copy(new NotFoundException("missing"), new LinkedHashMap<>()),
            "invalid", FastBeanCopier.copy(new ValidationException("field", "invalid"), new LinkedHashMap<>()),
            "reason", FastBeanCopier.copy(new I18nSupportException("reason"), new LinkedHashMap<>()));

        PlatformHolder restored = FastBeanCopier.copy(source, new PlatformHolder());

        Assert.assertTrue(restored.getMissing() instanceof NotFoundException.NoStackTrace);
        Assert.assertTrue(restored.getInvalid() instanceof ValidationException.NoStackTrace);
        Assert.assertTrue(restored.getReason() instanceof I18nSupportException.NoStackTrace);
    }

    @Test
    public void validationWithoutDetailsKeepsTheFieldAbsent() {
        for (ValidationException original : List.of(new ValidationException("reason"), new ValidationException.NoStackTrace("reason"))) {
            Map<String, Object> encoded = FastBeanCopier.copy(original, new LinkedHashMap<>());
            ValidationException restored = FastBeanCopier.copy(encoded, ValidationException.class);
            Assert.assertNull(restored.getDetails());
            Assert.assertFalse(FastBeanCopier.copy(restored, new LinkedHashMap<>()).containsKey("details"));
        }
    }

    @Test
    public void inconsistentNotFoundBusinessFieldsAreRejected() {
        for (Map<String, Object> invalid : List.of(
                Map.<String, Object>of("type", NotFoundException.class.getName(), "message", "missing", "status", 409),
                Map.<String, Object>of("type", NotFoundException.class.getName(), "message", "missing", "code", "conflict"))) {
            Assert.assertThrows(IllegalArgumentException.class, () -> FastBeanCopier.copy(invalid, Throwable.class));
        }
    }

    @Test
    public void beanAndRecordFieldsRestoreDescriptions() {
        Map<String, Object> source = Map.of("error", description());

        Throwable bean = FastBeanCopier.copy(source, new ThrowableHolder()).getError();
        BusinessException business = FastBeanCopier.copy(source, new BusinessHolder()).getError();
        Throwable record = FastBeanCopier.copy(source, ErrorRecord.class).error();

        Assert.assertEquals("failed", bean.getMessage());
        Assert.assertEquals("failed", business.getMessage());
        Assert.assertEquals("failed", record.getMessage());
        Assert.assertEquals(409, FastBeanCopier.copy(Map.of("error",
            Map.of("type", "remote.Business", "message", "failed", "status", 409)), new BusinessHolder()).getError().getStatus());
    }

    @Test
    public void jsonRoundTripRestoresBusinessDataAndTypedArguments() throws Exception {
        BusinessException original = new BusinessException("reason", "BUSINESS", 409, 12);
        Map<String, Object> encoded = FastBeanCopier.copy(original, new LinkedHashMap<>());
        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> decoded = mapper.readValue(mapper.writeValueAsString(Map.of("error", encoded)), Map.class);

        BusinessException restored = FastBeanCopier.copy(decoded, new BusinessHolder()).getError();

        Assert.assertEquals(original.getMessage(), restored.getMessage());
        Assert.assertEquals("reason", restored.getI18nCode());
        Assert.assertEquals("BUSINESS", restored.getCode());
        Assert.assertEquals(409, restored.getStatus());
        Assert.assertArrayEquals(new Object[]{12}, restored.getArgs());
        Assert.assertEquals(encoded.get("detail"), ((RemoteException) restored).getRemoteDetail());
    }

    @Test
    public void restoredBusinessRetainsSerializableMetadata() throws Exception {
        BusinessException original = new BusinessException("reason", "BUSINESS", 409, 12);
        Map<String, Object> encoded = FastBeanCopier.copy(original, new LinkedHashMap<>());
        BusinessException restored = FastBeanCopier.copy(encoded, BusinessException.class);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(restored);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            BusinessException decoded = (BusinessException) input.readObject();
            Assert.assertEquals(restored.getMessage(), decoded.getMessage());
            Assert.assertEquals("BUSINESS", decoded.getCode());
            Assert.assertEquals(409, decoded.getStatus());
            Assert.assertEquals(encoded.get("type"), ((RemoteExceptionMetadata) decoded).getRemoteType());
            Assert.assertEquals(encoded.get("detail"), ((RemoteExceptionMetadata) decoded).getRemoteDetail());
        }
    }

    @Test
    public void repeatedHopsRetainOriginalDescriptionAndMissingFields() {
        Map<String, Object> encoded = description();
        for (int i = 0; i < 3; i++) {
            Throwable restored = FastBeanCopier.copy(encoded, Throwable.class);
            encoded = FastBeanCopier.copy(restored, new LinkedHashMap<>());
            Assert.assertEquals(description(), encoded);
        }
        Throwable typeOnly = FastBeanCopier.copy(Map.of("type", "remote.Unknown"), Throwable.class);
        Assert.assertNull(typeOnly.getMessage());
        Assert.assertEquals(Map.of("type", "remote.Unknown"), FastBeanCopier.copy(typeOnly, new LinkedHashMap<>()));
    }

    @Test
    public void directTargetsReturnNewExceptionsAndHonorFiltering() {
        Throwable existing = new Throwable("local");

        Throwable restored = FastBeanCopier.copy(description(), existing);
        Assert.assertNotSame(existing, restored);
        Assert.assertEquals("failed", restored.getMessage());
        Assert.assertEquals("local", existing.getMessage());
        Assert.assertNull(((RemoteException) FastBeanCopier.copy(description(), Throwable.class, "detail")).getRemoteDetail());
        Assert.assertNull(((RemoteException) FastBeanCopier.copy(description(), new Throwable(),
            FastBeanCopier.include("type", "message"))).getRemoteDetail());
        Assert.assertEquals("failed", FastBeanCopier.DEFAULT_CONVERT.convert(description(), Throwable.class,
            FastBeanCopier.EMPTY_CLASS_ARRAY).getMessage());
    }

    @Test
    public void objectAndMapTargetsDoNotAutomaticallyRestoreDescriptions() {
        Map<String, Object> description = description();
        ObjectHolder holder = FastBeanCopier.copy(Map.of("error", description), new ObjectHolder());

        Assert.assertSame(description, holder.getError());
        Assert.assertSame(description, FastBeanCopier.copy(Map.of("error", description), new LinkedHashMap<>()).get("error"));
        Assert.assertSame(description, FastBeanCopier.DEFAULT_CONVERT.convert(description, Object.class,
            FastBeanCopier.EMPTY_CLASS_ARRAY));
    }

    @Test
    public void arraysAndListsPreserveArgumentTypesWithoutSharingTheContainer() {
        Object[] args = {12, "value"};
        Map<String, Object> source = new LinkedHashMap<>(description());
        source.put("args", args);
        RemoteException array = (RemoteException) FastBeanCopier.copy(source, Throwable.class);
        Assert.assertArrayEquals(args, array.getArgs());
        Assert.assertNotSame(args, array.getArgs());

        source.put("args", List.of(12, "value"));
        RemoteException list = (RemoteException) FastBeanCopier.copy(source, Throwable.class);
        Assert.assertArrayEquals(args, list.getArgs());
    }

    @Test
    public void untrustedTypeIsMetadataAndNeverInitializesTheNamedClass() {
        Throwable restored = FastBeanCopier.copy(Map.of("type", ClassNameTrap.class.getName(), "message", "remote"), Throwable.class);

        Assert.assertEquals(0, TrapCounter.initializations);
        Assert.assertEquals(ClassNameTrap.class.getName(), ((RemoteException) restored).getRemoteType());
    }

    @Test
    public void unsupportedTargetsAndIncompatibleLiveValuesFailExplicitly() {
        for (Class<? extends Throwable> target : List.of(IllegalStateException.class, Error.class)) {
            Assert.assertThrows(UnsupportedOperationException.class, () -> FastBeanCopier.copy(description(), target));
        }
        Assert.assertThrows(IllegalArgumentException.class,
            () -> FastBeanCopier.copy(new AssertionError("local"), RuntimeException.class));
        Assert.assertThrows(IllegalArgumentException.class,
            () -> FastBeanCopier.DEFAULT_CONVERT.convert("failed", Throwable.class, FastBeanCopier.EMPTY_CLASS_ARRAY));
    }

    @Test
    public void malformedDescriptionFieldsAreRejected() {
        for (Map<String, ?> invalid : List.<Map<String, ?>>of(Map.of(), Map.of("type", ""), Map.of("type", 1),
                Map.of("type", "remote", "message", 1), Map.of("type", "remote", "detail", 1),
                Map.of("type", "remote", "i18nCode", 1), Map.of("type", "remote", "code", 1),
                Map.of("type", "remote", "args", "value"), Map.of("type", "remote", "args", new int[]{1}))) {
            Assert.assertThrows(IllegalArgumentException.class, () -> FastBeanCopier.copy(invalid, Throwable.class));
        }
        for (Object details : List.of("invalid", List.of("invalid"), List.of(Map.of("property", 1)), List.of(Map.of("message", 1)))) {
            Assert.assertThrows(IllegalArgumentException.class,
                () -> FastBeanCopier.copy(Map.of("type", "remote", "details", details), Throwable.class));
        }
    }

    @Test
    public void statusRequiresAnExactIntAndAcceptsJsonNumberForms() {
        for (Object invalid : new Object[]{"409", 409.5, Long.MAX_VALUE, Double.NaN}) {
            Map<String, Object> source = new LinkedHashMap<>(description());
            source.put("status", invalid);
            Assert.assertThrows(IllegalArgumentException.class, () -> FastBeanCopier.copy(source, Throwable.class));
        }
        for (Number valid : List.of(409, 409L, new BigDecimal("409.0"))) {
            Map<String, Object> source = new LinkedHashMap<>(description());
            source.put("status", valid);
            Assert.assertEquals(409, FastBeanCopier.copy(source, BusinessException.class).getStatus());
        }
    }

    private Map<String, Object> description() {
        return Map.of("type", "remote.Problem", "message", "failed", "detail", "remote stack");
    }

    public static class ThrowableHolder {
        private Throwable error;
        public Throwable getError() { return error; }
        public void setError(Throwable error) { this.error = error; }
    }

    public static class BusinessHolder {
        private BusinessException error;
        public BusinessException getError() { return error; }
        public void setError(BusinessException error) { this.error = error; }
    }

    public static class ObjectHolder {
        private Object error;
        public Object getError() { return error; }
        public void setError(Object error) { this.error = error; }
    }

    public static class ErrorList {
        private List<Throwable> errors;
        public List<Throwable> getErrors() { return errors; }
        public void setErrors(List<Throwable> errors) { this.errors = errors; }
    }

    public static class PlatformHolder {
        private NotFoundException missing;
        private ValidationException invalid;
        private I18nSupportException reason;
        public NotFoundException getMissing() { return missing; }
        public void setMissing(NotFoundException missing) { this.missing = missing; }
        public ValidationException getInvalid() { return invalid; }
        public void setInvalid(ValidationException invalid) { this.invalid = invalid; }
        public I18nSupportException getReason() { return reason; }
        public void setReason(I18nSupportException reason) { this.reason = reason; }
    }

    public record ErrorRecord(Throwable error) {
    }

    public static class TrapCounter {
        static int initializations;
    }

    public static class ClassNameTrap extends RuntimeException {
        static { TrapCounter.initializations++; }
    }
}
