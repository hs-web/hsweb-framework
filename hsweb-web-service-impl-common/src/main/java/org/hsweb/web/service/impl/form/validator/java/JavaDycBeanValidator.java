/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.service.impl.form.validator.java;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.ezorm.exception.ValidationException;
import org.hsweb.ezorm.meta.expand.Validator;
import org.hsweb.web.bean.valid.ValidResults;
import org.hsweb.web.core.exception.BusinessException;
import org.springframework.util.ReflectionUtils;

import javax.validation.ConstraintViolation;
import java.lang.reflect.Field;
import java.util.*;

public class JavaDycBeanValidator implements Validator {

    private Class clazz;
    private javax.validation.Validator hibernateValidator;


    public JavaDycBeanValidator(Class clazz, javax.validation.Validator hibernateValidator) {
        this.clazz = clazz;
        this.hibernateValidator = hibernateValidator;
    }

    public boolean validateMap(Map<Object, Object> data, Operation operation) {
        ValidResults results = new ValidResults();
        try {
            Object validatorTarget = clazz.newInstance();
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
                    Field field = ReflectionUtils.findField(clazz, (String) key);
                    if (field != null)
                        result.addAll(hibernateValidator.validateValue(clazz, (String) key, value));
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
