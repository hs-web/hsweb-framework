package org.hswebframework.web.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.junit.Assert;
import org.junit.Test;

import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FastBeanCopierBeanTest {

    @Test
    public void testCopyDirectGenericListField() {
        Map<String, Object> source = Map.of(
            "fileData",
            List.of(Map.of("name", "image.jpg"))
        );

        DirectOutput output = FastBeanCopier.copy(source, new DirectOutput());

        Assert.assertNotNull(output.getFileData());
        Assert.assertEquals(1, output.getFileData().size());
        Assert.assertTrue(output.getFileData().get(0) instanceof FileItem);
        Assert.assertEquals("image.jpg", output.getFileData().get(0).getName());
    }

    @Test
    public void testCopyGenericSuperclassListField() {
        Map<String, Object> source = Map.of(
            "fileData",
            List.of(Map.of("name", "image.jpg"))
        );

        GenericOutput output = FastBeanCopier.copy(source, new GenericOutput());

        Assert.assertNotNull(output.getFileData());
        Assert.assertEquals(1, output.getFileData().size());
        Assert.assertTrue(output.getFileData().get(0) instanceof FileItem);
        Assert.assertEquals("image.jpg", output.getFileData().get(0).getName());
    }

    @Test
    public void testCopyChainAccessors() {
        Map<String, Object> values = Map.of("name", "chain", "age", 12, "enabled", true);
        ChainBean bean = FastBeanCopier.copy(values, new ChainBean());

        Assert.assertEquals("chain", bean.getName());
        Assert.assertEquals(12, bean.getAge());
        Assert.assertTrue(bean.isEnabled());
        Assert.assertEquals(values, FastBeanCopier.copy(bean, new LinkedHashMap<>()));

        StandardBean standard = FastBeanCopier.copy(bean, new StandardBean());
        Assert.assertEquals("chain", standard.getName());
        Assert.assertEquals(12, standard.getAge());
        ChainBean copied = FastBeanCopier.copy(standard, new ChainBean());
        Assert.assertEquals(values, FastBeanCopier.copy(copied, new LinkedHashMap<>()));
    }

    @Test
    public void testCopyFluentAccessors() {
        Map<String, Object> values = Map.of("name", "fluent", "age", 18, "enabled", true);
        FluentBean bean = FastBeanCopier.copy(values, new FluentBean());

        Assert.assertEquals("fluent", bean.name());
        Assert.assertEquals(18, bean.age());
        Assert.assertTrue(bean.enabled());
        Assert.assertEquals(values, FastBeanCopier.copy(bean, new LinkedHashMap<>()));

        StandardBean standard = FastBeanCopier.copy(bean, new StandardBean());
        Assert.assertEquals("fluent", standard.getName());
        Assert.assertEquals(18, standard.getAge());
        FluentBean copied = FastBeanCopier.copy(standard, new FluentBean());
        Assert.assertEquals(values, FastBeanCopier.copy(copied, new LinkedHashMap<>()));
    }

    @Test
    public void testCopyBetweenAccessorStylesAndIgnoreProperty() {
        ChainBean source = new ChainBean().setName("chain").setAge(20).setEnabled(true);
        FluentBean fluent = FastBeanCopier.copy(source, new FluentBean());
        Assert.assertEquals("chain", fluent.name());
        Assert.assertEquals(20, fluent.age());
        Assert.assertTrue(fluent.enabled());

        ChainBean target = FastBeanCopier.copy(fluent, new ChainBean().setName("retained"), "name");
        Assert.assertEquals("retained", target.getName());
        Assert.assertEquals(20, target.getAge());
        Assert.assertTrue(target.isEnabled());

        FluentBean converted = FastBeanCopier.copy(Map.of("age", "21"), new FluentBean());
        Assert.assertEquals(21, converted.age());
    }

    @Test
    public void testCopyInheritedFluentGenericList() {
        FluentOutput output = FastBeanCopier.copy(
            Map.of("fileData", List.of(Map.of("name", "fluent.jpg"))),
            new FluentOutput()
        );

        Assert.assertEquals(1, output.fileData().size());
        Assert.assertTrue(output.fileData().get(0) instanceof FileItem);
        Assert.assertEquals("fluent.jpg", output.fileData().get(0).getName());
    }

    @Test
    public void testIgnoreUnpairedStaticAndMismatchedFluentMethods() {
        Assert.assertTrue(FastBeanCopier.copy(new InvalidAccessors(), new LinkedHashMap<>()).isEmpty());
    }

    @Test
    public void testAccessorExtensionsDoNotChangeGlobalBeanUtils() throws Exception {
        PropertyUtilsBean global = BeanUtilsBean.getInstance().getPropertyUtils();
        PropertyDescriptor descriptor = global.getPropertyDescriptor(new ChainBean(), "name");
        Assert.assertNull(descriptor.getWriteMethod());

        Assert.assertEquals("isolated", FastBeanCopier.copy(Map.of("name", "isolated"), new ChainBean()).getName());

        Assert.assertNull(global.getPropertyDescriptor(new ChainBean(), "name").getWriteMethod());
    }

    @Test
    public void testChainSetterOverloadsMatchGetterType() {
        Map<String, Object> values = Map.of("name", "exact");

        Assert.assertEquals("exact", FastBeanCopier.copy(values, new OverloadedChainA()).getName());
        Assert.assertEquals("exact", FastBeanCopier.copy(values, new OverloadedChainB()).getName());
    }

    @Test
    public void testIgnoreStaticSetters() {
        Map<String, Object> values = Map.of("name", "updated");

        StaticChainSetter chain = FastBeanCopier.copy(values, new StaticChainSetter());
        StaticVoidSetter standard = FastBeanCopier.copy(values, new StaticVoidSetter());
        Assert.assertEquals("retained", chain.getName());
        Assert.assertEquals("retained", standard.getName());
        Assert.assertTrue(FastBeanCopier.copy(chain, new LinkedHashMap<>()).isEmpty());
        Assert.assertTrue(FastBeanCopier.copy(standard, new LinkedHashMap<>()).isEmpty());

        Assert.assertEquals("updated", FastBeanCopier.copy(values, new ChainWithStaticOverload()).getName());
    }

    public static class OverloadedChainA {
        private String name;

        public String getName() {
            return name;
        }

        public OverloadedChainA setName(Object name) {
            throw new AssertionError("Setter must match getter type");
        }

        public OverloadedChainA setName(String name) {
            this.name = name;
            return this;
        }
    }

    public static class OverloadedChainB {
        private String name;

        public String getName() {
            return name;
        }

        public OverloadedChainB setName(String name) {
            this.name = name;
            return this;
        }

        public OverloadedChainB setName(Object name) {
            throw new AssertionError("Setter must match getter type");
        }
    }

    public static class StaticChainSetter {
        public String getName() {
            return "retained";
        }

        public static StaticChainSetter setName(String name) {
            throw new AssertionError("Static setter must not be called");
        }
    }

    public static class StaticVoidSetter {
        public String getName() {
            return "retained";
        }

        public static void setName(String name) {
            throw new AssertionError("Static setter must not be called");
        }
    }

    public static class ChainWithStaticOverload extends ChainBean {
        public static ChainBean setName(Object name) {
            throw new AssertionError("Static overload must not be called");
        }
    }

    @Getter
    @Setter
    public static class StandardBean {
        private String name;
        private int age;
        private boolean enabled;

        public String name() {
            throw new AssertionError("Standard getter must take precedence");
        }

        public StandardBean name(String name) {
            throw new AssertionError("Standard setter must take precedence");
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ChainBean {
        private String name;
        private int age;
        private boolean enabled;
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static class FluentBean {
        private String name;
        private int age;
        private boolean enabled;

        public FluentBean name(String name) {
            this.name = name;
            return this;
        }

        public FluentBean name(Integer name) {
            throw new AssertionError("Setter must match getter type");
        }
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static class FluentGenericBase<T> {
        private List<T> fileData;
    }

    public static class FluentOutput extends FluentGenericBase<FileItem> {
    }

    public static class InvalidAccessors {
        public String readOnly() {
            throw new AssertionError("Unpaired method must not be copied");
        }

        public static String staticValue() {
            throw new AssertionError("Static method must not be copied");
        }

        public static InvalidAccessors staticValue(String value) {
            throw new AssertionError("Static method must not be copied");
        }

        public String mismatched() {
            throw new AssertionError("Mismatched method must not be copied");
        }

        public InvalidAccessors mismatched(Integer value) {
            throw new AssertionError("Mismatched method must not be copied");
        }
    }

    @Getter
    @Setter
    public static class DirectOutput {
        private List<FileItem> fileData;
    }

    @Getter
    @Setter
    public static class GenericBase<T> {
        private List<T> fileData;
    }

    public static class GenericOutput extends GenericBase<FileItem> {
    }

    @Getter
    @Setter
    public static class FileItem {
        private String name;
    }
}
