package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class FastBeanCopierConverterTest {

    private final FastBeanCopierBackend backend;
    private final ConvertUtilsBean convertUtils = BeanUtilsBean.getInstance().getConvertUtils();
    private final Map<Class<?>, org.apache.commons.beanutils.Converter> originalConverters = new LinkedHashMap<>();
    private FastBeanCopierBackend originalBackend;

    public FastBeanCopierConverterTest(String name, FastBeanCopierBackend backend) {
        this.backend = backend;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> backends() {
        return Arrays.asList(
            new Object[]{"javassist", new JavassistFastBeanCopierBackend()},
            new Object[]{"reflect", new ReflectFastBeanCopierBackend()},
            new Object[]{"reflection-accessor", new ReflectionAccessorFastBeanCopierBackend()},
            new Object[]{"asm-accessor", new AsmAccessorFastBeanCopierBackend()}
        );
    }

    @Before
    public void setUp() {
        originalBackend = FastBeanCopierSupport.getBackend();
        FastBeanCopierSupport.setBackend(backend);
        for (Class<?> type : Arrays.asList(Metadata.class, AbstractMetadata.class, ImmutableMetadata.class, DefaultMetadata.class)) {
            originalConverters.put(type, convertUtils.lookup(type));
            convertUtils.deregister(type);
        }
    }

    @After
    public void tearDown() {
        originalConverters.forEach((type, converter) -> {
            if (converter == null) {
                convertUtils.deregister(type);
            } else {
                convertUtils.register(converter, type);
            }
        });
        FastBeanCopierSupport.setBackend(originalBackend);
    }

    @Test
    public void shouldUseRegisteredConvertersForNestedMapProperties() {
        register(Metadata.class, value -> new DefaultMetadata(name(value)));
        register(AbstractMetadata.class, value -> new NamedMetadata(name(value)));
        register(ImmutableMetadata.class, value -> new ImmutableMetadata(name(value)));

        Definition target = FastBeanCopier.copy(Map.of(
            "params", metadata("interface"),
            "abstractParams", metadata("abstract"),
            "immutableParams", metadata("immutable")
        ), new Definition());

        Assert.assertEquals("interface", target.getParams().getName());
        Assert.assertEquals("abstract", target.getAbstractParams().getName());
        Assert.assertEquals("immutable", target.getImmutableParams().getName());
    }

    @Test
    public void shouldUseRegisteredConverterForBeanMapProperty() {
        register(Metadata.class, value -> new DefaultMetadata(name(value)));
        MapDefinition source = new MapDefinition();
        source.setParams(metadata("bean-map"));

        Definition target = FastBeanCopier.copy(source, new Definition());

        Assert.assertEquals("bean-map", target.getParams().getName());
    }

    @Test
    public void shouldUseRegisteredConverterForCollectionArrayAndRecordComponents() {
        register(Metadata.class, value -> new DefaultMetadata(name(value)));
        Map<String, Object> source = Map.of(
            "params", metadata("record"),
            "items", List.of(metadata("list")),
            "array", List.of(metadata("array"))
        );

        Definition target = FastBeanCopier.copy(source, new Definition());
        DefinitionRecord record = FastBeanCopier.copy(source, DefinitionRecord.class);

        Assert.assertEquals("list", target.getItems().get(0).getName());
        Assert.assertEquals("array", target.getArray()[0].getName());
        Assert.assertEquals("record", record.params().getName());
        Assert.assertEquals("list", record.items().get(0).getName());
    }

    @Test
    public void shouldUseExplicitConverterForNestedMapProperties() {
        Converter converter = new Converter() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Object source, Class<T> targetClass, Class[] genericTypes) {
                if (targetClass == Metadata.class) {
                    return (T) new DefaultMetadata("explicit-" + name(source));
                }
                return FastBeanCopier.DEFAULT_CONVERT.convert(source, targetClass, genericTypes);
            }
        };
        MapDefinition source = new MapDefinition();
        source.setParams(metadata("bean"));

        Definition fromMap = FastBeanCopier.copy(Map.of("params", metadata("map")), new Definition(), converter);
        Definition fromBean = FastBeanCopier.copy(source, new Definition(), converter);

        Assert.assertEquals("explicit-map", fromMap.getParams().getName());
        Assert.assertEquals("explicit-bean", fromBean.getParams().getName());
    }

    @Test
    public void shouldObserveRegistrationReplacementAndDeregistrationAfterCacheWarmup() {
        Map<String, Object> source = Map.of("params", metadata("original"));
        Assert.assertEquals("original", FastBeanCopier.copy(source, new MutableDefinition()).getParams().getName());

        register(DefaultMetadata.class, value -> new DefaultMetadata("registered"));
        Assert.assertEquals("registered", FastBeanCopier.copy(source, new MutableDefinition()).getParams().getName());

        register(DefaultMetadata.class, value -> new DefaultMetadata("replaced"));
        Assert.assertEquals("replaced", FastBeanCopier.copy(source, new MutableDefinition()).getParams().getName());

        convertUtils.deregister(DefaultMetadata.class);
        Assert.assertEquals("original", FastBeanCopier.copy(source, new MutableDefinition()).getParams().getName());
    }

    @Test
    public void shouldPreserveExistingPropertyWhenRegisteredConverterReturnsNull() {
        register(Metadata.class, value -> null);
        DefaultMetadata existing = new DefaultMetadata("existing");
        Definition target = new Definition();
        target.setParams(existing);

        FastBeanCopier.copy(Map.of("params", metadata("ignored")), target);

        Assert.assertSame(existing, target.getParams());
    }

    @Test
    public void shouldPropagateRegisteredConverterFailure() {
        ConversionException failure = new ConversionException("invalid metadata");
        register(Metadata.class, value -> {
            throw failure;
        });

        ConversionException actual = Assert.assertThrows(ConversionException.class,
            () -> FastBeanCopier.copy(Map.of("params", metadata("invalid")), new Definition()));

        Assert.assertSame(failure, actual);
    }

    private void register(Class<?> type, Function<Object, ?> conversion) {
        convertUtils.register(new org.apache.commons.beanutils.Converter() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Class<T> targetClass, Object value) {
                return (T) conversion.apply(value);
            }
        }, type);
    }

    private static Map<String, Object> metadata(String name) {
        return Map.of("name", name);
    }

    private static String name(Object value) {
        return (String) ((Map<?, ?>) value).get("name");
    }

    public interface Metadata {
        String getName();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultMetadata implements Metadata {
        private String name;
    }

    public abstract static class AbstractMetadata implements Metadata {
    }

    @Getter
    @AllArgsConstructor
    public static class NamedMetadata extends AbstractMetadata {
        private final String name;
    }

    @Getter
    @AllArgsConstructor
    public static class ImmutableMetadata implements Metadata {
        private final String name;
    }

    @Getter
    @Setter
    public static class Definition {
        private Metadata params;
        private AbstractMetadata abstractParams;
        private ImmutableMetadata immutableParams;
        private List<Metadata> items;
        private Metadata[] array;
    }

    @Getter
    @Setter
    public static class MapDefinition {
        private Map<String, Object> params;
    }

    @Getter
    @Setter
    public static class MutableDefinition {
        private DefaultMetadata params;
    }

    public record DefinitionRecord(Metadata params, List<Metadata> items) {
    }
}
