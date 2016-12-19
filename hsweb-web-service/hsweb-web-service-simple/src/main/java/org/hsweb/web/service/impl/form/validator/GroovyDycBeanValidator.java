package org.hsweb.web.service.impl.form.validator;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.hsweb.ezorm.core.Validator;
import org.hsweb.ezorm.rdb.exception.ValidationException;
import org.hsweb.web.bean.validator.ValidateResults;
import org.hsweb.web.core.exception.BusinessException;
import org.springframework.util.ReflectionUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;

import javax.validation.ConstraintViolation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 基于Groovy动态Bean对象验证
 * 原理，通过调用groovy脚本获取一个已编译的动态bean实例，然后进行赋值后验证
 * Created by 浩 on 2015-12-24 0024.
 */
public class GroovyDycBeanValidator implements Validator {

    protected static DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine("groovy");

    private String                     className;
    private javax.validation.Validator hibernateValidator;


    public GroovyDycBeanValidator(String className, javax.validation.Validator hibernateValidator) {
        this.className = className;
        this.hibernateValidator = hibernateValidator;
    }

    public boolean validateMap(Map<Object, Object> data, Operation operation) {
        ValidateResults results = new ValidateResults();
        try {
            Class validatorTargetClass = (Class) engine.execute(className, new HashMap<>()).getIfSuccess();
            Object validatorTarget = validatorTargetClass.newInstance();
            Set<ConstraintViolation<Object>> result = new LinkedHashSet<>();
            if (operation == Operation.INSERT) {
                data.forEach((key, value) -> {
                    try {
                        BeanUtils.setProperty(validatorTarget, (String) key, value);
                    } catch (Exception e) {
                    }
                });
                result.addAll(hibernateValidator.validate(validatorTarget));
            } else
                data.forEach((key, value) -> {
                    Field field = ReflectionUtils.findField(validatorTargetClass, (String) key);
                    if (field != null)
                        result.addAll(hibernateValidator.validateValue(validatorTargetClass, (String) key, value));
                });
            if (result.size() > 0) {
                for (ConstraintViolation<Object> violation : result) {
                    String property = violation.getPropertyPath().toString();
                    results.addResult(property, violation.getMessage());
                }
            }
        } catch (Exception e) {
            throw new BusinessException("验证器异常！", e, 500);
        }
        if (results.size() > 0) throw new ValidationException(results.get(0).getMessage(), results);
        return true;
    }

    @Override
    public boolean validate(Object data, Operation operation) throws ValidationException {
        if (data instanceof Map)
            return validateMap(((Map) data), operation);
        if (data instanceof Collection) {
            for (Object o : ((Collection) data)) {
                validate(o, operation);
            }
        } else {
            BeanMap beanMap = new BeanMap();
            beanMap.setBean(data);
            validateMap(beanMap, operation);
        }
        return true;
    }

}
