package org.hswebframework.web;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class Sqls {

    public static List<String> parse(String sqlText) {
        String[] list = sqlText.split("[\n]");
        List<String> sqlList = new ArrayList<>();
        List<String> tmp = new ArrayList<>();
        Stream.of(list)
                .filter(s -> !s.startsWith("--") && s.trim().length() != 0)
                .forEach(s1 -> {
                    if (s1.trim().endsWith(";")) {
                        s1 = s1.trim();
                        tmp.add(s1.substring(0, s1.length() - 1));
                        sqlList.add(String.join("\n", tmp.toArray(new String[tmp.size()])));
                        tmp.clear();
                    } else {
                        tmp.add(s1);
                    }
                });
        sqlList.add(String.join("\n", tmp.toArray(new String[tmp.size()])));
        tmp.clear();
        return sqlList;
    }
}
