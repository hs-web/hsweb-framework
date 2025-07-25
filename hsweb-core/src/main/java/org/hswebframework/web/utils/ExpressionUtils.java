package org.hswebframework.web.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 表达式工具,用户解析表达式为字符串
 *
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
@Deprecated
public class ExpressionUtils {

    //表达式提取正则 ${.+?}
    private static final Pattern PATTERN = Pattern.compile("(?<=\\$\\{)(.+?)(?=})");

    /**
     * 获取默认的表达式变量
     *
     * @return 变量集合
     */
    public static Map<String, Object> getDefaultVar() {
        return new HashMap<>();
    }

    /**
     * 获取默认的表达式变量并将制定的变量合并在一起
     *
     * @param var 要合并的变量集合
     * @return 变量集合
     */
    public static Map<String, Object> getDefaultVar(Map<String, Object> var) {
        Map<String, Object> vars = getDefaultVar();
        vars.putAll(var);
        return vars;
    }

    /**
     * 使用默认的变量解析表达式
     *
     * @param expression 表达式字符串
     * @param language   表达式语言
     * @return 解析结果
     * @throws Exception 解析错误
     * @see ExpressionUtils#analytical(String, Map, String)
     */
    public static String analytical(String expression, String language) throws Exception {
        return analytical(expression, new HashMap<>(), language);
    }

    /**
     * 解析表达式,表达式使用{@link ExpressionUtils#PATTERN}进行提取<br>
     * 如调用 analytical("http://${3+2}/test",var,"spel")<br>
     * 支持的表达式语言:
     * <ul>
     * <li>freemarker</li>
     * <li>spel</li>
     * <li>ognl</li>
     * <li>groovy</li>
     * <li>js</li>
     * </ul>
     *
     * @param expression 表达式字符串
     * @param vars       变量
     * @param language   表达式语言
     * @return 解析结果
     */
    @SneakyThrows
    public static String analytical(String expression, Map<String, Object> vars, String language) {
        if (!expression.contains("${")) {
            return expression;
        }


        return TemplateParser.parse(expression, var -> {
            if (ObjectUtils.isEmpty(var)) {
                return "";
            }
            Object val = vars.get(var);
            if (val != null) {
                return String.valueOf(val);
            }
            return "";

        });
    }

}
