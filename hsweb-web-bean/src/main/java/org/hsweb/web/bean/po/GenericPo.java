package org.hsweb.web.bean.po;


import org.hsweb.web.bean.valid.ValidResults;
import org.webbuilder.utils.common.MD5;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.Serializable;
import java.util.Set;

/**
 * 通用的PO对象，实现基本的属性和方法。新建的PO都应继承该类
 * Created by 浩 on 2015-07-20 0020.
 */
public class GenericPo<PK> implements Serializable {
    private static final long serialVersionUID = 9197157871004374522L;
    /**
     * 主键
     */
    private PK u_id;

    public PK getU_id() {
        return u_id;
    }

    @Override
    public int hashCode() {
        if (getU_id() == null) return 0;
        return getU_id().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    public void setU_id(PK u_id) {
        this.u_id = u_id;
    }

    /**
     * 创建一个主键，根据当前时戳和随机数的一个MD5值
     *
     * @return
     */
    public static String createUID() {
        return MD5.encode(String.valueOf(System.nanoTime()) + String.valueOf(Math.random()));
    }

    /**
     * 使用hibernate验证器验证一个对象
     *
     * @param object 需要验证的对象
     * @return 验证结果
     */
    public static final ValidResults valid(Object object) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> set = validator.validate(object);
        ValidResults results = new ValidResults();
        if (set.size() != 0) {
            for (ConstraintViolation<Object> violation : set) {
                results.addResult(violation.getPropertyPath().toString(), violation.getMessage());
            }
        }
        return results;
    }

    /**
     * 验证自生
     *
     * @return 验证结果
     */
    public ValidResults valid() {
        return valid(this);
    }

}
