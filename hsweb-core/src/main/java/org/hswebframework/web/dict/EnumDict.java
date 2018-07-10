package org.hswebframework.web.dict;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 枚举字典,使用枚举来实现数据字典,可通过集成此接口来实现一些有趣的功能.
 * ⚠️:如果使用了位运算来判断枚举,枚举数量不要超过64个,且顺序不要随意变动!
 * 如果枚举数量大于64,你应该使用{@link org.hswebframework.web.dict.apply.DictApply}来处理.
 * ⚠️:如果要开启在反序列化json的时候,支持将对象反序列化枚举,由于fastJson目前的版本还不支持从父类获取注解,
 * 所以需要在实现类上注解:<code>@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)</code>.
 *
 * @author zhouhao
 * @see 3.0
 * @see EnumDictJSONDeserializer
 * @see JSONSerializable
 */
@JSONType(deserializer = EnumDict.EnumDictJSONDeserializer.class)
public interface EnumDict<V> extends JSONSerializable {

    /**
     * 枚举选项的值,通常由字母或者数字组成,并且在同一个枚举中值唯一;对应数据库中的值通常也为此值
     *
     * @return 枚举的值
     * @see ItemDefine#getValue()
     */
    V getValue();

    /**
     * 枚举字典选项的文本,通常为中文
     *
     * @return 枚举的文本
     * @see ItemDefine#getText()
     */
    String getText();

    /**
     * {@link Enum#ordinal}
     *
     * @return 枚举序号, 如果枚举顺序改变, 此值将被变动
     */
    int ordinal();

    default long index() {
        return ordinal();
    }

    default long getMask() {
        return 1L << index();
    }

    /**
     * 对比是否和value相等,对比地址,值,value转为string忽略大小写对比,text忽略大小写对比
     *
     * @param v value
     * @return 是否相等
     */
    @SuppressWarnings("all")
    default boolean eq(Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof Object[]) {
            v = Arrays.asList(v);
        }
        if (v instanceof Collection) {
            return ((Collection) v).stream().anyMatch(this::eq);
        }
        if (v instanceof Map) {
            v = ((Map) v).getOrDefault("value", ((Map) v).get("text"));
        }
        return this == v
                || getValue() == v
                || getValue().equals(v)
                || (v instanceof Number ? in(((Number) v).longValue()) : false)
                || String.valueOf(getValue()).equalsIgnoreCase(String.valueOf(v))
                || v.equals(getMask())
                || getText().equalsIgnoreCase(String.valueOf(v)
        );
    }

    default boolean in(long mask) {
        return (mask & getMask()) != 0;
    }

    /**
     * 枚举选项的描述,对一个选项进行详细的描述有时候是必要的.默认值为{@link this#getText()}
     *
     * @return 描述
     */
    default String getComments() {
        return getText();
    }

    /**
     * 从指定的枚举类中查找想要的枚举,并返回一个{@link Optional},如果未找到,则返回一个{@link Optional#empty()}
     *
     * @param type      实现了{@link EnumDict}的枚举类
     * @param predicate 判断逻辑
     * @param <T>       枚举类型
     * @return 查找到的结果
     */
    static <T extends Enum & EnumDict> Optional<T> find(Class<T> type, Predicate<T> predicate) {
        if (type.isEnum()) {
            for (T enumDict : type.getEnumConstants()) {
                if (predicate.test(enumDict)) {
                    return Optional.of(enumDict);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 根据枚举的{@link EnumDict#getValue()}来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum & EnumDict<?>> Optional<T> findByValue(Class<T> type, Object value) {
        return find(type, e -> e.getValue() == value || e.getValue().equals(value) || String.valueOf(e.getValue()).equalsIgnoreCase(String.valueOf(value)));
    }

    /**
     * 根据枚举的{@link EnumDict#getText()} 来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum & EnumDict> Optional<T> findByText(Class<T> type, String text) {
        return find(type, e -> e.getText().equalsIgnoreCase(text));
    }

    /**
     * 根据枚举的{@link EnumDict#getValue()},{@link EnumDict#getText()}来查找.
     *
     * @see this#find(Class, Predicate)
     */
    static <T extends Enum & EnumDict> Optional<T> find(Class<T> type, Object target) {
        return find(type, v -> v.eq(target));
    }

    @SafeVarargs
    static <T extends EnumDict> long toMask(T... t) {
        if (t == null) {
            return 0L;
        }
        long value = 0L;
        for (T t1 : t) {
            value |= t1.getMask();
        }
        return value;
    }

    @SafeVarargs
    static <T extends EnumDict> boolean maskIn(long mask, T... t) {
        long value = toMask(t);
        return (mask & value) == value;
    }

    @SafeVarargs
    static <T extends EnumDict> boolean maskInAny(long mask, T... t) {
        long value = toMask(t);
        return (mask & value) != 0;
    }

    static <T extends EnumDict> List<T> getByMask(List<T> allOptions, long mask) {
        if (allOptions.size() >= 64) {
            throw new UnsupportedOperationException("不支持选项超过64个数据字典!");
        }
        List<T> arr = new ArrayList<>();
        List<T> all = allOptions;
        for (T t : all) {
            if (t.in(mask)) {
                arr.add(t);
            }
        }
        return arr;
    }

    static <T extends EnumDict> List<T> getByMask(Supplier<List<T>> allOptionsSupplier, long mask) {
        return getByMask(allOptionsSupplier.get(), mask);
    }


    static <T extends Enum & EnumDict> List<T> getByMask(Class<T> tClass, long mask) {

        return getByMask(Arrays.asList(tClass.getEnumConstants()), mask);
    }

    /**
     * 默认在序列化为json时,默认会以对象方式写出枚举,可通过系统环境变量 <code>hsweb.enum.dict.disableWriteJSONObject</code>关闭默认设置。
     * 比如: java -jar -Dhsweb.enum.dict.disableWriteJSONObject=true
     */
    boolean DEFAULT_WRITE_JSON_OBJECT = !Boolean.getBoolean("hsweb.enum.dict.disableWriteJSONObject");

    /**
     * @return 是否在序列化为json的时候, 将枚举以对象方式序列化
     * @see this#DEFAULT_WRITE_JSON_OBJECT
     */
    default boolean isWriteJSONObjectEnabled() {
        return DEFAULT_WRITE_JSON_OBJECT;
    }

    /**
     * 当{@link this#isWriteJSONObjectEnabled()}返回true时,在序列化为json的时候,会写出此方法返回的对象
     *
     * @return 最终序列化的值
     * @see this#isWriteJSONObjectEnabled()
     */
    default Object getWriteJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", getValue());
        jsonObject.put("text", getText());
        jsonObject.put("index", index());
        jsonObject.put("mask", getMask());
        return jsonObject;
    }

    @Override
    default void write(JSONSerializer jsonSerializer, Object o, Type type, int i) throws IOException {
        if (isWriteJSONObjectEnabled()) {
            jsonSerializer.write(getWriteJSONObject());
        } else {
            jsonSerializer.write(getValue());
        }
    }

    /**
     * 自定义fastJson枚举序列化
     */
    class EnumDictJSONDeserializer implements ObjectDeserializer {

        @Override
        @SuppressWarnings("all")
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            try {
                Object value;
                final JSONLexer lexer = parser.lexer;
                final int token = lexer.token();
                if (token == JSONToken.LITERAL_INT) {
                    int intValue = lexer.intValue();
                    lexer.nextToken(JSONToken.COMMA);

                    return (T) EnumDict.find((Class) type, intValue);
                } else if (token == JSONToken.LITERAL_STRING) {
                    String name = lexer.stringVal();
                    lexer.nextToken(JSONToken.COMMA);

                    if (name.length() == 0) {
                        return (T) null;
                    }
                    return (T) EnumDict.find((Class) type, name).orElse(null);
                } else if (token == JSONToken.NULL) {
                    lexer.nextToken(JSONToken.COMMA);
                    return null;
                } else {
                    value = parser.parse();
                    if (value instanceof Map) {
                        return (T) EnumDict.find(((Class) type), ((Map) value).get("value"))
                                .orElseGet(() ->
                                        EnumDict.find(((Class) type), ((Map) value).get("text")).orElse(null));
                    }
                }

                throw new JSONException("parse enum " + type + " error, value : " + value);
            } catch (JSONException e) {
                throw e;
            } catch (Exception e) {
                throw new JSONException(e.getMessage(), e);
            }
        }

        @Override
        public int getFastMatchToken() {
            return JSONToken.LITERAL_STRING;
        }
    }

}
