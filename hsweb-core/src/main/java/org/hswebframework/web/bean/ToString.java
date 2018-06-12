package org.hswebframework.web.bean;

import org.springframework.util.ClassUtils;

import java.lang.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class ToString {

    public static long DEFAULT_FEATURE = Feature.createFeatures(
            Feature.coverIgnoreProperty
            , Feature.nullPropertyToEmpty
//            , Feature.jsonFormat
    );

    public static final Map<Class, ToStringOperator> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("all")
    public static <T> ToStringOperator<T> getOperator(Class<T> type) {
        return cache.computeIfAbsent(type, DefaultToStringOperator::new);
    }

    @SuppressWarnings("all")
    public static <T> String toString(T target) {
        return getOperator((Class<T>) ClassUtils.getUserClass(target)).toString(target);
    }

    @SuppressWarnings("all")
    public static <T> String toString(T target, String... ignoreProperty) {
        return getOperator((Class<T>) ClassUtils.getUserClass(target)).toString(target, ignoreProperty);
    }

    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Ignore {

        String[] value() default {};

        boolean cover() default true;

    }

    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Features {
        Feature[] value() default {};
    }

    public enum Feature {

        /**
         * 什么也不配置
         *
         * @since 3.0.0-RC
         */
        empty,

        /**
         * 忽略为null的字段
         *
         * @since 3.0.0-RC
         */
        ignoreNullProperty,

        /**
         * null的字段转为空,如null字符串转为"", null的list转为[]
         *
         * @since 3.0.0-RC
         */
        nullPropertyToEmpty,

        /**
         * 排除的字段使用*进行遮盖,如: 张三 =? 张* , 18502314087 => 185****087
         *
         * @since 3.0.0-RC
         */
        coverIgnoreProperty,

        /**
         * 是否关闭嵌套属性toString
         *
         * @since 3.0.0-RC
         */
        disableNestProperty,

        /**
         * 以json方式进行格式化
         *
         * @since 3.0.0-RC
         */
        jsonFormat,

        /**
         * 是否写出类名
         *
         * @since 3.0.0-RC
         */
        writeClassname;


        public long getMask() {
            return 1L << ordinal();
        }

        public static boolean hasFeature(long features, Feature feature) {
            long mast = feature.getMask();
            return (features & mast) == mast;
        }

        public static long removeFeatures(long oldFeature, Feature... features) {
            if (features == null) {
                return 0L;
            }
            long value = oldFeature;
            for (Feature feature : features) {
                value &= ~feature.getMask();
            }
            return value;
        }

        public static long createFeatures(Feature... features) {
            if (features == null) {
                return 0L;
            }
            long value = 0L;
            for (Feature feature : features) {
                value |= feature.getMask();
            }

            return value;
        }
    }

}
