package org.hswebframework.web.starter;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class ValidatorTests {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @Test
    public void validate() {
        TestBean bean=new TestBean();

        Set<ConstraintViolation<TestBean>> violations= factory.getValidator().validate(bean);

        Assert.assertTrue(violations.size()==2);

        for (ConstraintViolation<TestBean> violation : violations) {
            System.out.println(violation.getPropertyPath()+ violation.getMessage());
        }

    }

    public static class TestBean {

        @Range(max = 99)
        private int range = 100;

        @NotNull
        private String notNull="";

        @NotBlank
        public String getString() {

            return "";
        }
    }
}
