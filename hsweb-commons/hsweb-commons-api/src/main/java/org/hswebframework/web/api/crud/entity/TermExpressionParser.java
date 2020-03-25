package org.hswebframework.web.api.crud.entity;

import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.Sort;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 动态条件表达式解析器
 * name=测试 and age=test
 *
 * @author zhouhao
 * @since 3.0.10
 */
public class TermExpressionParser {

    public static List<Term> parse(String expression) {
        Query<?, QueryParamEntity> conditional = QueryParamEntity.newQuery();

        NestConditional<?> nest = null;

        // 字符容器
        char[] buf = new char[128];
        // 记录词项的长度, Arrays.copyOf使用
        byte len = 0;
        // 空格数量?
        byte spaceLen = 0;
        // 当前列
        char[] currentColumn = null;
        // 当前列对应的值
        char[] currentValue = null;
        // 当前条件类型 eq btw in ...
        String currentTermType = null;
        // 当前链接类型 and / or
        String currentType = "and";
        // 是否是引号, 单引号 / 双引号
        byte quotationMarks = 0;
        // 表达式字符数组
        char[] all = expression.toCharArray();

        for (char c : all) {

            if (c == '\'' || c == '"') {
                if (quotationMarks != 0) {
                    // 碰到(结束的)单/双引号, 标志归零, 跳过
                    quotationMarks = 0;
                    continue;
                }
                // 碰到(开始的)单/双引号, 做记录, 跳过
                quotationMarks++;
                continue;
            } else if (c == '(') {
                nest = (nest == null ?
                        (currentType.equals("or") ? conditional.orNest() : conditional.nest()) :
                        (currentType.equals("or") ? nest.orNest() : nest.nest()));
                len = 0;
                continue;
            } else if (c == ')') {
                if (nest == null) {
                    continue;
                }
                if (null != currentColumn) {
                    currentValue = Arrays.copyOf(buf, len);
                    nest.accept(new String(currentColumn), convertTermType(currentTermType), new String(currentValue));
                    currentColumn = null;
                    currentTermType = null;
                }
                Object end = nest.end();
                nest = end instanceof NestConditional ? ((NestConditional) end) : null;
                len = 0;
                spaceLen++;
                continue;
            } else if (c == '=' || c == '>' || c == '<') {
                if (currentTermType != null) {
                    currentTermType += String.valueOf(c);
                    //spaceLen--;
                } else {
                    currentTermType = String.valueOf(c);
                }

                if (currentColumn == null) {
                    currentColumn = Arrays.copyOf(buf, len);
                }
                spaceLen++;
                len = 0;
                continue;
            } else if (c == ' ') {
                if (len == 0) {
                    continue;
                }
                if (quotationMarks != 0) {
                    // 如果当前字符是空格，并且前面迭代时碰到过单/双引号, 不处理并且添加到buf中
                    buf[len++] = c;
                    continue;
                }
                spaceLen++;
                if (currentColumn == null && (spaceLen == 1 || spaceLen % 5 == 0)) {
                    currentColumn = Arrays.copyOf(buf, len);
                    len = 0;
                    continue;
                }
                if (null != currentColumn) {
                    if (null == currentTermType) {
                        currentTermType = new String(Arrays.copyOf(buf, len));
                        len = 0;
                        continue;
                    }
                    currentValue = Arrays.copyOf(buf, len);
                    if (nest != null) {
                        nest.accept(new String(currentColumn), convertTermType(currentTermType), new String(currentValue));
                    } else {
                        conditional.accept(new String(currentColumn), convertTermType(currentTermType), new String(currentValue));
                    }
                    currentColumn = null;
                    currentTermType = null;
                    len = 0;
                    continue;
                } else if (len == 2 || len == 3) {
                    String type = new String(Arrays.copyOf(buf, len));
                    if (type.equalsIgnoreCase("or")) {
                        currentType = "or";
                        if (nest != null) {
                            nest.or();
                        } else {
                            conditional.or();
                        }
                        len = 0;
                        continue;
                    } else if (type.equalsIgnoreCase("and")) {
                        currentType = "and";
                        if (nest != null) {
                            nest.and();
                        } else {
                            conditional.and();
                        }
                        len = 0;
                        continue;
                    } else {
                        currentColumn = Arrays.copyOf(buf, len);
                        len = 0;
                        spaceLen++;
                    }
                } else {
                    currentColumn = Arrays.copyOf(buf, len);
                    len = 0;
                    spaceLen++;
                }
                continue;
            }

            buf[len++] = c;
        }
        if (null != currentColumn) {
            currentValue = Arrays.copyOf(buf, len);
            if (nest != null) {
                nest.accept(new String(currentColumn), convertTermType(currentTermType), new String(currentValue));
            } else {
                conditional.accept(new String(currentColumn), convertTermType(currentTermType), new String(currentValue));
            }
        }
        return conditional.getParam().getTerms();
    }

    /**
     * 解析排序表达式
     * <pre>
     *     age asc,score desc
     * </pre>
     *
     * @param expression 表达式
     * @return 排序集合
     * @since 4.0.1
     */
    public static List<Sort> parseOrder(String expression) {
        return Stream.of(expression.split("[,]"))
                .map(str -> str.split("[ ]"))
                .map(arr -> {
                    Sort sort = new Sort();
                    sort.setName(arr[0]);
                    if (arr.length > 1 && "desc".equalsIgnoreCase(arr[1])) {
                        sort.desc();
                    }
                    return sort;
                }).collect(Collectors.toList());
    }

    private static String convertTermType(String termType) {
        if (termType == null) {
            return TermType.eq;
        }
        switch (termType) {
            case "=":
                return TermType.eq;
            case ">":
                return TermType.gt;
            case "<":
                return TermType.lt;
            case ">=":
                return TermType.gte;
            case "<=":
                return TermType.lte;
            default:
                return termType;
        }

    }
}
