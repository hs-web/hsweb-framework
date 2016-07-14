package org.hsweb.web.service.impl.form.validator;

import org.hsweb.ezorm.meta.FieldMetaData;
import org.hsweb.ezorm.meta.TableMetaData;
import org.hsweb.ezorm.meta.expand.Validator;
import org.hsweb.ezorm.meta.expand.ValidatorFactory;
import org.hsweb.web.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.hsweb.commons.StringUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;

import java.util.HashMap;
import java.util.Map;
@Component
public class GroovyDycBeanValidatorFactory implements ValidatorFactory {

    @Autowired
    private javax.validation.Validator hibernateValidator;

    private Map<String, Validator> base = new HashMap<>();

    private static final Map<Class, String> simpleType = new HashMap<>();

    protected static DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine("groovy");

    public static final String basePackage = "org.hsweb.web.bean.dyn_form";

    private static final String[] imports = {
            "import org.hibernate.validator.constraints.*;",
            "import javax.validation.constraints.*;"
    };

    static {
        simpleType.put(Integer.class, "int");
        simpleType.put(Long.class, "long");
        simpleType.put(String.class, "String");
        simpleType.put(Double.class, "double");
        simpleType.put(Float.class, "float");
        simpleType.put(Boolean.class, "boolean");
        simpleType.put(Short.class, "short");
        simpleType.put(Byte.class, "byte");
        simpleType.put(Character.class, "char");
    }

    @Override
    public Validator createValidator(TableMetaData metaData) {
        return initValidator(metaData);
    }

    /**
     * 根据表结构动态生成一个基于hibernate-validator的bean
     *
     * @param metaData 表结构
     * @return 验证器对象
     */
    public Validator initValidator(TableMetaData metaData) {
        StringBuilder script = new StringBuilder();
        String className = StringUtils.concat(basePackage, ".", metaData.getName());
        script.append("package ").append(basePackage).append(";\n");
        for (String anImport : imports) {
            script.append(anImport).append("\n");
        }
        script.append("public class ").append(metaData.getName()).append("{\n");
        boolean hasValidator = false;
        for (FieldMetaData fieldMetaData : metaData.getFields()) {
            String typeName = simpleType.get(fieldMetaData.getJavaType());
            if (typeName == null) typeName = fieldMetaData.getJavaType().getName();
            if (fieldMetaData.getValidator() == null || fieldMetaData.getValidator().isEmpty()) continue;
            for (String ann : fieldMetaData.getValidator()) {
                hasValidator = true;
                script.append("\t@").append(ann).append("\n");
            }
            script.append("\tprivate ")
                    .append(typeName).append(" ")
                    .append(fieldMetaData.getName()).append(";\n\n");
        }
        //没有配置验证器
        if (!hasValidator) return null;
        for (FieldMetaData fieldMetaData : metaData.getFields()) {
            String typeName = simpleType.get(fieldMetaData.getJavaType());
            if (typeName == null) typeName = fieldMetaData.getJavaType().getName();
            if (fieldMetaData.getValidator() == null || fieldMetaData.getValidator().isEmpty()) continue;
            script.append("public ")
                    .append(typeName).append(" get")
                    .append(StringUtils.toUpperCaseFirstOne(fieldMetaData.getName()))
                    .append("(){\n")
                    .append("\treturn this.").append(fieldMetaData.getName()).append(";")
                    .append("\n}\n");
            script.append("public void set").append(StringUtils.toUpperCaseFirstOne(fieldMetaData.getName()))
                    .append("(")
                    .append(typeName).append(" ").append(fieldMetaData.getName())
                    .append(")")
                    .append("{\n")
                    .append("\tthis.").append(fieldMetaData.getName()).append("=").append(fieldMetaData.getName()).append(";")
                    .append("\n}\n\n");
        }
        script.append("}");
        try {
            engine.compile(className, script.toString());
        } catch (Exception e) {
            throw new BusinessException("创建动态表单验证器失败!", e, 500);
        }
        GroovyDycBeanValidator validator = new GroovyDycBeanValidator(className, hibernateValidator);
        base.put(metaData.getName(), validator);
        return validator;
    }


}
