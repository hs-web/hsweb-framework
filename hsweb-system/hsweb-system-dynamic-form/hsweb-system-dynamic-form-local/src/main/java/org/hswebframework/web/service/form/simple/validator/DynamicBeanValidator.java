package org.hswebframework.web.service.form.simple.validator;

import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.ezorm.core.Validator;
import org.hswebframework.web.Maps;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.bean.ValidateBean;
import org.hswebframework.web.proxy.Proxy;
import org.hswebframework.web.validator.group.CreateGroup;
import org.hswebframework.web.validator.group.UpdateGroup;

import javax.validation.GroupSequence;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public class DynamicBeanValidator implements Validator {

    protected Supplier<ValidateBean> beanSupplier;

    public static void main(String[] args) {
        MapBean bean = Proxy.create(MapBean.class)
                .addField("private String name;", NotBlank.class, Maps.<String, Object>buildMap()
                        .put("message", "测试")
                        .put("groups", new Class[]{CreateGroup.class})
                        .get())
                .addMethod("public String getName(){return this.name;}")
                .addMethod("public java.util.Set keySet(){return new java.util.HashSet(java.util.Arrays.asList(new String[]{\"name\"}));}")
                .addMethod("public void setName(String name){  this.name=name;}")
                .addMethod("public void setProperty(String name,Object value){  this.name=(String)value;}")
                .addMethod("public Object getProperty(String name){ return this.name;}")
                .newInstance();


        bean.setProperty("name", "test");

        System.out.println((MapBean) bean.tryValidate(CreateGroup.class));

    }

    @Override
    public boolean validate(Object o, Operation operation) {
        ValidateBean validateBean = beanSupplier.get();
        FastBeanCopier.copy(o, validateBean);
        if (operation == Operation.INSERT) {
            validateBean.tryValidate(CreateGroup.class);
        } else {
            validateBean.tryValidate(UpdateGroup.class);
        }
        return true;
    }
}
