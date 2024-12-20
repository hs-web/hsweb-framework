package org.hswebframework.web.api.crud.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExtensibleEntityTest {


    @Test
    @SneakyThrows
    public void testJson() {
        ExtensibleEntity<String> entity = new ExtensibleEntity<>();
        entity.setId("test");
        entity.setExtension("extName", "test");

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writerFor(ExtensibleEntity.class).writeValueAsString(entity);

        System.out.println(json);
        ExtensibleEntity<String> decoded = mapper.readerFor(ExtensibleEntity.class).readValue(json);
        assertNotNull(decoded.getId());

        assertEquals(entity.getId(), decoded.getId());

        assertNotNull(decoded.getExtension("extName"));

        assertEquals(entity.getExtension("extName"), decoded.getExtension("extName"));
    }
}