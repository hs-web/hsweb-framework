package org.hswebframework.web.system.authorization.defaults.webflux;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.EntityFactoryHolder;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.simple.SimpleDimensionType;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

public class DimensionTypeResponseTest {
    private Object previous;

    @Before
    public void isolateFactory() {
        previous = ReflectionTestUtils.getField(EntityFactoryHolder.class, "FACTORY");
        ReflectionTestUtils.setField(EntityFactoryHolder.class, "FACTORY", null);
    }

    @After
    public void restoreFactory() {
        ReflectionTestUtils.setField(EntityFactoryHolder.class, "FACTORY", previous);
    }

    @Test
    public void keepsStaticResponseAndTwoArgumentFactory() {
        DimensionTypeResponse response = DimensionTypeResponse.of(SimpleDimensionType.of("org", "Organization"));
        assertEquals(DimensionTypeResponse.class, response.getClass());
        assertEquals("org", response.getId());
        assertEquals("Organization", response.getName());
        assertEquals("Role", DimensionTypeResponse.of("role", "Role").getName());
    }

    @Test
    public void copiesExtendedMetadataThroughRegisteredEntityFactory() {
        MapperEntityFactory factory = new MapperEntityFactory();
        factory.addMapping(DimensionTypeResponse.class, ExtendedResponse::new);
        ReflectionTestUtils.setField(EntityFactoryHolder.class, "FACTORY", factory);
        ExtendedType type = new ExtendedType();
        type.setId("business"); type.setName("Business"); type.setDescription("Business dimension");
        DimensionTypeResponse mapped = DimensionTypeResponse.of(type);
        assertTrue(mapped instanceof ExtendedResponse);
        ExtendedResponse response = (ExtendedResponse) mapped;
        assertEquals("business", response.getId());
        assertEquals("Business", response.getName());
        assertEquals("Business dimension", response.getDescription());
    }

    @Getter @Setter
    public static class ExtendedType implements DimensionType {
        private String id;
        private String name;
        private String description;
    }

    @Getter @Setter
    public static class ExtendedResponse extends DimensionTypeResponse {
        private String description;
    }
}
