package org.hsweb.web.bean.validator;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 对象验证结果集合
 * Created by 浩 on 2015-10-10 0010.
 */
public class ValidateResults extends ArrayList<ValidateResults.ValidResult> implements Serializable {
    private static final long    serialVersionUID = 8910856253780046561L;
    /**
     * 是否验证通过
     */
    private              boolean success          = true;

    @Override
    public boolean addAll(Collection<? extends ValidResult> c) {
        success = false;
        return super.addAll(c);
    }

    @Override
    public boolean add(ValidResult result) {
        success = false;
        return super.add(result);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public void addResult(String field, String message) {
        this.add(new ValidResult(field, message));
    }

    /**
     * 单个属性验证结果
     */
    public class ValidResult {
        public ValidResult() {
        }

        /**
         * 带参数构造方法，用于初始化验证的字段和验证的结果
         *
         * @param field   验证的字段
         * @param message 验证结果
         */
        public ValidResult(String field, String message) {
            this.field = field;
            this.message = message;
        }

        private String field;
        private String message;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("{\"%s\":\"%s\"}", getField(), getMessage());
        }
    }

    public boolean isSuccess() {
        return success;
    }
}
