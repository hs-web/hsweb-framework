package org.hsweb.web.service.impl.form.validator;

import org.hsweb.web.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.webbuilder.sql.FieldMetaData;
import org.webbuilder.sql.TableMetaData;
import org.webbuilder.sql.validator.Validator;
import org.webbuilder.sql.validator.ValidatorFactory;
import org.webbuilder.utils.common.StringUtils;
import org.webbuilder.utils.script.engine.DynamicScriptEngine;
import org.webbuilder.utils.script.engine.DynamicScriptEngineFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于groovy动态bean的校验器
 */
@Component
public class GroovyDycBeanValidatorFactory implements ValidatorFactory {

    @Autowired
    private javax.validation.Validator hibernateValidator;

    private Map<String, Validator> base = new HashMap<>();

    protected static DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine("groovy");

    public static final String basePackage = "org.hsweb.web.bean.dyn_form";

    private static final String[] imports = {
            "import org.hibernate.validator.constraints.*;",
            "import javax.validation.constraints.*;"
    };

    @Override
    public Validator getValidator(TableMetaData metaData) {
        return base.get(metaData.getName());
    }

    /**
     * 根据表结构动态生成一个基于hibernate-validator的bean
     *
     * @param metaData 表结构
     * @return 验证器对象
     */
    @Override
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
            if(fieldMetaData.getValidator().isEmpty())continue;
            for (String ann : fieldMetaData.getValidator()) {
                hasValidator = true;
                script.append("\t@").append(ann).append("\n");
            }
            script.append("\tprivate ")
                    .append(fieldMetaData.getJavaType().getName()).append(" ")
                    .append(fieldMetaData.getName()).append(";\n\n");
        }
        //没有配置验证器
        if (!hasValidator) return null;
        for (FieldMetaData fieldMetaData : metaData.getFields()) {
            if(fieldMetaData.getValidator().isEmpty())continue;
            script.append("public ")
                    .append(fieldMetaData.getJavaType().getName()).append(" get")
                    .append(StringUtils.toUpperCaseFirstOne(fieldMetaData.getName()))
                    .append("(){\n")
                    .append("\treturn this.").append(fieldMetaData.getName()).append(";")
                    .append("\n}\n");
            script.append("public void set").append(StringUtils.toUpperCaseFirstOne(fieldMetaData.getName()))
                    .append("(")
                    .append(fieldMetaData.getJavaType().getName()).append(" ").append(fieldMetaData.getName())
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
