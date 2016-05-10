package org.hsweb.web.service.impl.form.validator;

import org.hsweb.web.bean.valid.ValidResults;
import org.hsweb.web.core.exception.BusinessException;
import org.webbuilder.sql.validator.Validator;
import org.webbuilder.utils.common.BeanUtils;
import org.webbuilder.utils.common.ClassUtils;
import org.webbuilder.utils.script.engine.DynamicScriptEngine;
import org.webbuilder.utils.script.engine.DynamicScriptEngineFactory;

import javax.validation.ConstraintViolation;
import javax.xml.bind.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于Groovy动态Bean对象验证
 * 原理，通过调用groovy脚本获取一个已编译的动态bean实例，然后进行赋值后验证
 * Created by 浩 on 2015-12-24 0024.
 */
public class GroovyDycBeanValidator implements Validator {

    protected static DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine("groovy");

    private String className;
    private javax.validation.Validator hibernateValidator;


    public GroovyDycBeanValidator(String className, javax.validation.Validator hibernateValidator) {
        this.className = className;
        this.hibernateValidator = hibernateValidator;
    }

    public List valid(Object data, boolean insert) {
        ValidResults results = new ValidResults();
        try {
            if (!(data instanceof Map)) {
                throw new ValidationException("数据类型错误!");
            }
            Object bean = engine.execute(className + ".getInstance", new HashMap<>()).getResult();
            Map<String, Object> mapData = ((Map) data);
            for (Map.Entry<String, Object> entry : mapData.entrySet()) {
                BeanUtils.attr(bean, entry.getKey(), entry.getValue());
            }
            Set<ConstraintViolation<Object>> result = hibernateValidator.validate(bean);
            if (result.size() > 0) {
                for (ConstraintViolation<Object> violation : result) {
                    String property = violation.getPropertyPath().toString();
                    if (insert) {
                        results.addResult(property, violation.getMessage());
                    } else if (mapData.containsKey(property)) {
                        results.addResult(property, violation.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException("验证器异常！", e, 500);
        }
        if (results.size() > 0) throw new BusinessException(results.toString(), 400);
        return null;
        //采用异常通知方式
    }

    @Override
    public List insertValid(Object data) {
        return valid(data, true);
    }

    @Override
    public List updateValid(Object data) {
        return valid(data, false);
    }
}
