package org.hswebframework.web.service.form.simple.validator;

import org.hswebframework.ezorm.core.Validator;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.bean.ValidateBean;
import org.hswebframework.web.validator.group.CreateGroup;
import org.hswebframework.web.validator.group.UpdateGroup;

import java.util.function.Supplier;

public class DynamicBeanValidator implements Validator {

    protected Supplier<ValidateBean> beanSupplier;

    public DynamicBeanValidator(Supplier<ValidateBean> beanSupplier) {
        this.beanSupplier = beanSupplier;
    }

    @Override
    public boolean validate(Object source, Operation operation) {
        ValidateBean validateBean = beanSupplier.get();
        FastBeanCopier.copy(source, validateBean);
        if (operation == Operation.INSERT) {
            validateBean.tryValidate(CreateGroup.class);
        } else {
            validateBean.tryValidate(UpdateGroup.class);
        }
        return true;
    }
}
