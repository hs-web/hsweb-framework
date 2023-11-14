package org.hswebframework.web.crud.events.expr;

import io.netty.util.concurrent.FastThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.mapping.EntityColumnMapping;
import org.hswebframework.web.crud.query.QueryHelperUtils;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;
import reactor.function.Function3;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SpelSqlExpressionInvoker extends AbstractSqlExpressionInvoker {

    protected static class SqlFunctions extends HashMap<String, Object> {

        private final EntityColumnMapping mapping;

        public SqlFunctions(EntityColumnMapping mapping, Map<String, Object> map) {
            super(map);
            this.mapping = mapping;
        }

        @Override
        public Object get(Object key) {
            Object val = super.get(key);
            if (val == null) {
                val = super.get(QueryHelperUtils.toHump(String.valueOf(key)));
            }
            if (val == null) {
                val = mapping
                        .getPropertyByColumnName(String.valueOf(key))
                        .map(super::get)
                        .orElse(null);
            }
            return val;
        }

        public String lower(Object str) {
            return String.valueOf(str).toLowerCase();
        }

        public String upper(Object str) {
            return String.valueOf(str).toUpperCase();
        }

        public Object ifnull(Object nullable, Object val) {
            return nullable == null ? val : nullable;
        }

        public String substring(Object str, int start, int length) {
            return String.valueOf(str).substring(start, length);
        }

        public String trim(Object str) {
            return String.valueOf(str).trim();
        }

        public String concat(Object... args) {
            StringBuilder builder = new StringBuilder();
            for (Object arg : args) {
                builder.append(arg);
            }
            return builder.toString();
        }

        public Object coalesce(Object... args) {
            for (Object arg : args) {
                if (arg != null) {
                    return arg;
                }
            }
            return null;
        }
    }

    static final FastThreadLocal<StandardEvaluationContext> SHARED_CONTEXT = new FastThreadLocal<StandardEvaluationContext>() {
        @Override
        protected StandardEvaluationContext initialValue() {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.addPropertyAccessor(accessor);
            context.addMethodResolver(new ReflectiveMethodResolver() {
                @Override
                public MethodExecutor resolve(@Nonnull EvaluationContext context,
                                              @Nonnull Object targetObject,
                                              @Nonnull String name,
                                              @Nonnull List<TypeDescriptor> argumentTypes) throws AccessException {
                    return super.resolve(context, targetObject, name.toLowerCase(), argumentTypes);
                }
            });
            context.setOperatorOverloader(new OperatorOverloader() {
                @Override
                public boolean overridesOperation(@Nonnull Operation operation, Object leftOperand, Object rightOperand) throws EvaluationException {
                    if (leftOperand instanceof Number || rightOperand instanceof Number) {
                        return leftOperand == null || rightOperand == null;
                    }
                    return leftOperand == null && rightOperand == null;
                }

                @Override
                public Object operate(@Nonnull Operation operation, Object leftOperand, Object rightOperand) throws EvaluationException {
                    return null;
                }
            });
            return context;
        }
    };

    @Override
    protected Function3<EntityColumnMapping, Object[], Map<String, Object>, Object> compile(String sql) {

        StringBuilder builder = new StringBuilder(sql.length());
        int argIndex = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '?') {
                builder.append("_arg").append(argIndex++);
            } else {
                builder.append(c);
            }
        }
        try {
            SpelExpressionParser parser = new SpelExpressionParser();

            Expression expression = parser.parseExpression(builder.toString());
            AtomicLong errorCount = new AtomicLong();

            return (mapping, args, object) -> {
                if (errorCount.get() > 1024) {
                    return null;
                }
                object = createArguments(mapping, object);

                if (args != null && args.length != 0) {
                    int index = 0;
                    for (Object parameter : args) {
                        object.put("_arg" + index, parameter);
                    }
                }
                StandardEvaluationContext context = SHARED_CONTEXT.get();
                try {
                    context.setRootObject(object);
                    Object val = expression.getValue(context);
                    errorCount.set(0);
                    return val;
                } catch (Throwable err) {
                    log.warn("invoke native sql [{}] value error",
                             sql,
                             err);
                    errorCount.incrementAndGet();
                } finally {
                    context.setRootObject(null);
                }
                return null;
            };
        } catch (Throwable error) {
            return spelError(sql, error);
        }
    }

    protected SqlFunctions createArguments(EntityColumnMapping mapping, Map<String, Object> args) {
        return new SqlFunctions(mapping, args);
    }

    protected Function3<EntityColumnMapping, Object[], Map<String, Object>, Object> spelError(String sql, Throwable error) {
        log.warn("create sql expression [{}] parser error", sql, error);
        return (mapping, args, data) -> null;
    }

    static ExtMapAccessor accessor = new ExtMapAccessor();

    static class ExtMapAccessor extends MapAccessor {
        @Override
        public boolean canRead(@Nonnull EvaluationContext context, Object target, @Nonnull String name) throws AccessException {
            return target instanceof Map;
        }

        @Override
        @Nonnull
        public TypedValue read(@Nonnull EvaluationContext context, Object target, @Nonnull String name) throws AccessException {
            Assert.state(target instanceof Map, "Target must be of type Map");
            Map<?, ?> map = (Map<?, ?>) target;
            Object value = map.get(name);
            return new TypedValue(value);
        }
    }

}
