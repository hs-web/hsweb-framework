package org.hswebframework.web.api.crud.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExtendableEntityTest {


    @Test
    @SneakyThrows
    public void testJson() {
        ExtendableEntity<String> entity = new ExtendableEntity<>();
        entity.setId("test");
        entity.setExtension("extName", "test");

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writerFor(ExtendableEntity.class).writeValueAsString(entity);

        System.out.println(json);
        ExtendableEntity<String> decoded = mapper.readerFor(ExtendableEntity.class).readValue(json);
        assertNotNull(decoded.getId());

        assertEquals(entity.getId(), decoded.getId());

        assertNotNull(decoded.getExtension("extName"));

        assertEquals(entity.getExtension("extName"), decoded.getExtension("extName"));
    }
}