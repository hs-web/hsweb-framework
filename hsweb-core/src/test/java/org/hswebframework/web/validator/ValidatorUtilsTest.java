package org.hswebframework.web.validator;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.i18n.LocaleUtils;
import org.junit.Test;

import javax.validation.constraints.NotBlank;

import java.util.Locale;

import static org.junit.Assert.*;

public class ValidatorUtilsTest {


    @Test
    public void test(){
        test(Locale.CHINA,"不能为空");
        test(Locale.ENGLISH,"must not be blank");
    }

    public void test(Locale locale,String msg){
        try {
            LocaleUtils.doWith(locale,en->{
                ValidatorUtils.tryValidate(new TestEntity());
            });
            throw new IllegalStateException();
        }catch (ValidationException e){
            assertEquals(msg,e.getDetails().get(0).getMessage());
        }
    }

    @Getter
    @Setter
    public static class TestEntity{

        @NotBlank
        private String notBlank;
    }
}