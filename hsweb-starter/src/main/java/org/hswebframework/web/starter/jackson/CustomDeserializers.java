package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import lombok.AllArgsConstructor;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.dict.EnumDict;

import java.io.IOException;

/** 为已注册实体和字典枚举选择 Jackson 解析器，实例缓存及生命周期由 Jackson 管理。 */
@AllArgsConstructor
@SuppressWarnings("all")
public class CustomDeserializers extends SimpleDeserializers {
    private final EntityFactory entityFactory;
    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                    DeserializationConfig config,
                                                    BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer<?> deserializer = super.findBeanDeserializer(type, config, beanDesc);
        if (deserializer != null) {
            return deserializer;
        }
        Class<?> clazz = entityFactory.getInstanceType(type.getRawClass(), false);
        if (clazz == null || clazz == type.getRawClass()) {
            return null;
        }
        // 由 Jackson 缓存解析器，不在共享 Module 查找期间修改注册表。
        return new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                // 只绑定当前位置，避免嵌套属性触发文档级尾随内容校验。
                return ctxt.readValue(p, clazz);
            }
        };
    }

    @Override
    public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                                                    DeserializationConfig config,
                                                    BeanDescription beanDesc) {
        JsonDeserializer<?> deser = null;
        if (type.isEnum() && EnumDict.class.isAssignableFrom(type)) {
            deser = new EnumDict.EnumDictDeserializer(val -> EnumDict
                .find((Class) type, val)
                .orElse(null));
        }
        return deser;
    }
}
