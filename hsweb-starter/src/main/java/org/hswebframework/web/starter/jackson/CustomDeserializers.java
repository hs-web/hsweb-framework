package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import org.hswebframework.web.api.crud.entity.EntityFactoryHolder;
import org.hswebframework.web.dict.EnumDict;

import java.io.IOException;

@SuppressWarnings("all")
public class CustomDeserializers extends SimpleDeserializers {
    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                    DeserializationConfig config,
                                                    BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer<?> deserializer = super.findBeanDeserializer(type, config, beanDesc);

        if (deserializer == null) {

            Class<?> clazz = EntityFactoryHolder.get().getInstanceType(type.getRawClass(), false);

            if (clazz == null || clazz == type.getRawClass()) {
                return null;
            }
            addDeserializer((Class) type.getRawClass(), new JsonDeserializer<Object>() {
                @Override
                public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                    return p.readValueAs(clazz);
                }
            });
        }

        return super.findBeanDeserializer(type, config, beanDesc);
    }

    @Override
    public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                                                    DeserializationConfig config,
                                                    BeanDescription beanDesc) {
        JsonDeserializer<?> deser = null;
        if (type.isEnum() && EnumDict.class.isAssignableFrom(type)) {
            deser = new EnumDict.EnumDictJSONDeserializer(val -> EnumDict
                .find((Class) type, val)
                .orElse(null));
        }
        return deser;
    }
}
