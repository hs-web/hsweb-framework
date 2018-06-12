package org.hswebframework.web.validator;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 逻辑主键验证器
 *
 * @see LogicPrimaryKey
 * @see Result
 */
public interface LogicPrimaryKeyValidator {

    <T> Result<T> validate(T bean, Class... group);

    @Getter
    @Setter
    class Result<T> {

        public static Result passed() {
            Result result = new Result();
            result.setError(false);
            return result;
        }

        private boolean error=false;

        private T data;

        private Map<String, Object> properties;

        private String message = "存在相同数据";

        public boolean isPassed() {
            return !error;
        }

        public void ifPassed(Consumer<Result<T>> consumer) {
            if (isPassed()) {
                consumer.accept(this);
            }
        }

        public void ifError(Consumer<Result<T>> consumer) {
            if (isError()) {
                consumer.accept(this);
            }
        }

        public void ifErrorThrow() {
            ifErrorThrow(DuplicateKeyException::new);
        }

        @SneakyThrows
        public void ifErrorThrow(Function<Result<T>, Exception> exceptionGetter) {
            if (isError()) {
                throw exceptionGetter.apply(this);
            }
        }
    }
}
