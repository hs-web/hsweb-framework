package org.hswebframework.web.api.crud.entity;

import lombok.SneakyThrows;
import org.apache.commons.collections4.MapUtils;
import org.hswebframework.ezorm.core.NestConditional;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.core.param.Sort;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;

import java.net.URLDecoder;
import java.util.*;
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

    /**
     * 解析Map为动态条件,map中的key为条件列,value为条件值,如果列以$or$开头则表示or查询.
     *
     * <pre>{@code
     *   {
     *       "name$like":"测试",
     *       //OR
     *       "$or$status$in":[1,2,3],
     *       //嵌套
     *       "$nest":{
     *           "age$gt":10,
     *       }
     *   }
     * }</pre>
     *
     * @param map map
     * @return 条件
     */
    public static List<Term> parse(Map<String, Object> map) {
        if (MapUtils.isEmpty(map)) {
            return Collections.emptyList();
        }

        List<Term> terms = new ArrayList<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            boolean isOr = false;
            Term term = new Term();

            //嵌套
            if (key.startsWith("$nest") ||
                    (isOr = key.startsWith("$orNest"))) {
                @SuppressWarnings("all")
                List<Term> nest = value instanceof Map ? parse(((Map<String, Object>) value)) : parse(String.valueOf(value));
                term.setTerms(nest);
            }
            //普通
            else {
                if (key.startsWith("$or$")) {
                    isOr = true;
                    key = key.substring(4);
                }
                term.setColumn(key);
                term.setValue(value);
            }

            if (isOr) {
                term.setType(Term.Type.or);
            }
            terms.add(term);
        }

        return terms;

    }

    @SneakyThrows
    public static List<Term> parse(String expression) {
        try {
            expression = URLDecoder.decode(expression, "utf-8");
        } catch (Throwable ignore) {

        }
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
