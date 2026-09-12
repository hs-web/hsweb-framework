package org.hswebframework.web.bean.accessor;

/**
 * 属性访问器工厂，用于为指定属性创建 reader/writer。
 *
 * @author zhouhao
 * @since 5.0.2
 */
public interface PropertyAccessor {

    PropertyReader createReader(Class<?> clazz, String name);

    PropertyWriter createWriter(Class<?> clazz, String name, TypeConverter typeConverter);

    default PropertyTransfer createTransfer(Class<?> sourceClass,
                                            String sourceName,
                                            boolean sourcePrimitive,
                                            Class<?> targetClass,
                                            String targetName) {
        return null;
    }

    default ConverterPropertyTransfer createConverterTransfer(Class<?> sourceClass,
                                                              String sourceName,
                                                              boolean sourcePrimitive,
                                                              Class<?> targetClass,
                                                              String targetName,
                                                              boolean allowDirectAssignment) {
        return null;
    }
}
