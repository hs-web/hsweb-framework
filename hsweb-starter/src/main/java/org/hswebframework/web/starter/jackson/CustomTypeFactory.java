package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.fasterxml.jackson.databind.util.LookupCache;
import org.hswebframework.web.api.crud.entity.EntityFactory;

public class CustomTypeFactory extends TypeFactory {

    private EntityFactory entityFactory;

    public CustomTypeFactory(EntityFactory factory) {
        super(new LRUMap<>(64, 1024));
        this.entityFactory = factory;
    }

    protected CustomTypeFactory(LookupCache<Object, JavaType> typeCache, TypeParser p,
                                TypeModifier[] mods, ClassLoader classLoader) {
        super(typeCache, p, mods, classLoader);
    }


    @Override
    public TypeFactory withCache(LRUMap<Object, JavaType> cache) {
        return new CustomTypeFactory(cache, _parser, _modifiers, _classLoader);
    }

    @Override
    public TypeFactory withClassLoader(ClassLoader classLoader) {
        return new CustomTypeFactory(_typeCache, _parser, _modifiers, _classLoader);
    }

    @Override
    public TypeFactory withModifier(TypeModifier mod) {
        LookupCache<Object, JavaType> typeCache = _typeCache;
        TypeModifier[] mods;
        if (mod == null) { // mostly for unit tests
            mods = null;
            // 30-Jun-2016, tatu: for some reason expected semantics are to clear cache
            //    in this case; can't recall why, but keeping the same
            typeCache = null;
        } else if (_modifiers == null) {
            mods = new TypeModifier[]{mod};
            // 29-Jul-2019, tatu: Actually I think we better clear cache in this case
            //    as well to ensure no leakage occurs (see [databind#2395])
            typeCache = null;
        } else {
            // but may keep existing cache otherwise
            mods = ArrayBuilders.insertInListNoDup(_modifiers, mod);
        }
        return new CustomTypeFactory(typeCache, _parser, mods, _classLoader);
    }

    @Override
    protected JavaType _fromWellKnownInterface(ClassStack context, Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
        JavaType javaType = super._fromWellKnownInterface(context, rawType, bindings, superClass, superInterfaces);
        if (javaType == null) {
            rawType = entityFactory.getInstanceType(rawType, false);
            if (rawType != null) {
                javaType = SimpleType.constructUnsafe(rawType);
            }
        }
        return javaType;
    }

    @Override
    protected JavaType _fromWellKnownClass(ClassStack context, Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {

        JavaType javaType = super._fromWellKnownClass(context, rawType, bindings, superClass, superInterfaces);
        if (javaType == null) {
            rawType = entityFactory.getInstanceType(rawType, false);
            if (rawType != null) {
                javaType = SimpleType.constructUnsafe(rawType);
            }
        }

        return javaType;
    }


}
