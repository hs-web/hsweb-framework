package org.hswebframework.web.bean;

import org.junit.Assert;
import org.junit.Test;

import java.beans.Transient;
import java.util.LinkedHashMap;
import java.util.Map;

public class FastBeanCopierTransientTest {

    @Test
    public void mapExportSkipsTransientFieldsAndBeanPropertiesBeforeReading() {
        ExportSource source = new ExportSource();

        Assert.assertEquals(Map.of("name", "exported", "visible", "kept"),
                            FastBeanCopier.copy(source, new LinkedHashMap<>()));
        Assert.assertEquals(0, source.inheritedReads);
        Assert.assertEquals(0, source.errorReads);
    }

    @Test
    public void includeCannotExportTransientProperties() {
        ExportSource source = new ExportSource();

        Assert.assertTrue(FastBeanCopier.copy(source, new LinkedHashMap<>(),
            FastBeanCopier.include("error", "inherited", "diagnostic", "setterMarked")).isEmpty());
        Assert.assertEquals(0, source.inheritedReads);
        Assert.assertEquals(0, source.errorReads);
    }

    @Test
    public void defaultConverterUsesTheSameMapExportRules() {
        Map<?, ?> map = FastBeanCopier.DEFAULT_CONVERT.convert(new ExportSource(), Map.class,
            FastBeanCopier.EMPTY_CLASS_ARRAY);

        Assert.assertEquals(Map.of("name", "exported", "visible", "kept"), map);
    }

    @Test
    public void beanAndRecordCopyRetainTransientValues() {
        TransferBean source = new TransferBean();
        source.setToken("local");

        Assert.assertEquals("local", FastBeanCopier.copy(source, new TransferBean()).getToken());
        Assert.assertEquals(new TokenRecord("local"), FastBeanCopier.copy(source, TokenRecord.class));
        Assert.assertEquals("local",
                            FastBeanCopier.copy(Map.of("token", "local"), new TransferBean()).getToken());
        Assert.assertEquals("local", FastBeanCopier.getProperty(source, "token"));
    }

    @Test
    public void recordExportHonorsTransientAccessor() {
        Assert.assertEquals(Map.of("name", "record"),
                            FastBeanCopier.copy(new ExportRecord("record", "local"), new LinkedHashMap<>()));
    }

    @Test
    public void mapKeysAreNotFilteredByPropertyNames() {
        Map<String, Object> source = Map.of("error", "value", "token", "value");

        Assert.assertEquals(source, FastBeanCopier.copy(source, new LinkedHashMap<>()));
    }

    @Test
    public void transientFalseCannotOverrideTransientField() {
        Assert.assertTrue(FastBeanCopier.copy(new TransferBean(), new LinkedHashMap<>()).isEmpty());
    }

    public static class ExportBase {
        private transient String inherited = "local";
        int inheritedReads;

        public String getInherited() {
            inheritedReads++;
            return inherited;
        }
    }

    public static class ExportSource extends ExportBase {
        private transient Throwable error;
        int errorReads;

        public String getName() {
            return "exported";
        }

        public Throwable getError() {
            errorReads++;
            throw new AssertionError("Transient error getter must not run");
        }

        @Transient
        public String getDiagnostic() {
            throw new AssertionError("Transient property getter must not run");
        }

        public String getSetterMarked() {
            throw new AssertionError("Setter-marked property getter must not run");
        }

        @Transient
        public void setSetterMarked(String value) {
        }

        @Transient(false)
        public String getVisible() {
            return "kept";
        }
    }

    public static class TransferBean {
        private transient String token = "local";

        @Transient(false)
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public record TokenRecord(String token) {
    }

    public record ExportRecord(String name, String diagnostic) {
        @Transient
        @Override
        public String diagnostic() {
            throw new AssertionError("Transient record accessor must not run");
        }
    }
}
