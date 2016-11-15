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

package org.hsweb.web.bean.validator.constraintvalidators;

import org.hsweb.commons.StringUtils;
import org.hsweb.web.bean.validator.constraints.NotMessyCode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MessyCodeValidator implements ConstraintValidator<NotMessyCode, String> {

    @Override
    public void initialize(NotMessyCode notMessyCode) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (null == s) return true;
        return !StringUtils.isMessyCode(s);
    }


}
