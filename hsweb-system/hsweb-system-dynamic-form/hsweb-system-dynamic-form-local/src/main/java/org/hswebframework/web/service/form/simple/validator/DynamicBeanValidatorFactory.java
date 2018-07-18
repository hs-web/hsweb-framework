package org.hswebframework.web.service.form.simple.validator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.Validator;
import org.hswebframework.ezorm.core.ValidatorFactory;
import org.hswebframework.ezorm.core.meta.ColumnMetaData;
import org.hswebframework.ezorm.core.meta.TableMetaData;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.proxy.Proxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hswebframework.web.proxy.Proxy.createMemberValue;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
@Slf4j
public class DynamicBeanValidatorFactory implements ValidatorFactory {

    @Autowired
    private List<JSR303AnnotationParserStrategy> strategies;

    private String createSetPropertyCode(TableMetaData tableMetaData) {
        StringBuilder builder = new StringBuilder();
        builder.append("public void setProperty(String property,Object value){\n");
        int index = 0;
        for (ColumnMetaData column : tableMetaData.getColumns()) {
            String propertyName = column.getAlias();
            Class type = column.getJavaType();
            if (index++ > 0) {
                builder.append("\nelse ");
            }
            builder.append("if(property.intern()==\"")
                    .append(propertyName)
                    .append("\"||property.intern()==\"")
                    .append(column.getName()).append("\"){\n")
                    .append("this.set")
                    .append(StringUtils.toUpperCaseFirstOne(propertyName))
                    .append("((").append(type.getName()).append(")")
                    .append("org.hswebframework.web.bean.FastBeanCopier.DEFAULT_CONVERT.convert(value,")
                    .append(type.getName())
                    .append(".class,null));")
                    .append("\n}");
        }
        builder.append("}");
        return builder.toString();
    }

    private String createGetPropertyCode(TableMetaData tableMetaData) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        builder.append("public Object getProperty(String property){\n");
        for (ColumnMetaData column : tableMetaData.getColumns()) {
            String propertyName = column.getAlias();
            if (index++ > 0) {
                builder.append("\nelse ");
            }
            builder.append("if(property.intern()==\"")
                    .append(propertyName)
                    .append("\"||property.intern()==\"")
                    .append(column.getName())
                    .append("\"){\n")
                    .append("return this.get")
                    .append(StringUtils.toUpperCaseFirstOne(propertyName))
                    .append("();")
                    .append("\n}");

        }
        builder.append("\nreturn null;\n}");
        return builder.toString();
    }


    protected List<JSR303AnnotationInfo> createValidatorAnnotation(Set<String> config) {
        if (CollectionUtils.isEmpty(config)) {
            return new java.util.ArrayList<>();
        }

        return config.stream()
                .map(this::createValidatorAnnotation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected JSR303AnnotationInfo createValidatorAnnotation(String config) {
        //JSON
        if (config.startsWith("{")) {
            JSONObject jsonConfig = JSON.parseObject(config);
            String type = jsonConfig.getString("type");
            return strategies.stream().filter(strategy -> strategy.support(type))
                    .findFirst()
                    .map(strategy -> strategy.parse(jsonConfig))
                    .orElse(null);
        }

        return null;
    }

    @Override
    public Validator createValidator(TableMetaData tableMetaData) {
        Proxy<MapBean> proxy = Proxy.create(MapBean.class);
        StringBuilder keySet = new StringBuilder("public java.util.Set keySet(){\n return new java.util.HashSet(java.util.Arrays.asList(new String[]{");
        int index = 0;
        for (ColumnMetaData column : tableMetaData.getColumns()) {
            String propertyName = column.getAlias();
            Class type = column.getJavaType();
            String typeName = type.getName();

            if (index++ > 0) {
                keySet.append(",");
            }

            keySet.append("\"")
                    .append(propertyName)
                    .append("\"");

            proxy.custom(ctClass -> {
                try {
                    CtField ctField = CtField.make("private " + type.getName() + " " + propertyName + ";", ctClass);
                    List<JSR303AnnotationInfo> jsr303 = createValidatorAnnotation(column.getValidator());
                    //添加注解
                    if (!CollectionUtils.isEmpty(jsr303)) {
                        ConstPool constPool = ctClass.getClassFile().getConstPool();
                        AnnotationsAttribute attributeInfo = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                        for (JSR303AnnotationInfo jsr303AnnotationInfo : jsr303) {
                            Class<? extends java.lang.annotation.Annotation> jsr303Ann = jsr303AnnotationInfo.getAnnotation();
                            Annotation ann = new javassist.bytecode.annotation.Annotation(jsr303Ann.getName(), constPool);
                            if (!CollectionUtils.isEmpty(jsr303AnnotationInfo.getProperties())) {
                                jsr303AnnotationInfo.getProperties().forEach((key, value) -> {
                                    MemberValue memberValue = createMemberValue(value, constPool);
                                    if (memberValue != null) {
                                        ann.addMemberValue(key, memberValue);
                                    }
                                });
                            }
                            attributeInfo.addAnnotation(ann);
                        }
                        ctField.getFieldInfo().addAttribute(attributeInfo);
                    }
                    ctClass.addField(ctField);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            proxy.addMethod("public void set" + StringUtils.toUpperCaseFirstOne(propertyName) + "(" + typeName + " " + propertyName + "){\n" +
                    "this." + propertyName + "=" + propertyName + ";\n" +
                    "\n};");

            proxy.addMethod("public " + typeName + " get" + StringUtils.toUpperCaseFirstOne(propertyName) + "(){\n" +
                    "return this." + propertyName + ";\n" +
                    "\n};");
        }

        keySet.append("}));\n}");

        proxy.addMethod(keySet.toString());
        proxy.addMethod(createSetPropertyCode(tableMetaData));
        proxy.addMethod(createGetPropertyCode(tableMetaData));

        //尝试一下能否创建实例
        MapBean mapBean = proxy.newInstance();
        Assert.notNull(mapBean, "创建验证器失败!");
        return new DynamicBeanValidator(proxy::newInstance);
    }
}
