package org.hswebframework.web.bean;

import org.hswebframework.utils.StringUtils;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

public abstract class CompareUtils {

    public static boolean compare(Object source, Object target) {
        if (source == target) {
            return true;
        }

        if (source == null || target == null) {
            return false;
        }

        if (source.equals(target)) {
            return true;
        }
        if (source instanceof Number) {
            return compare(((Number) source), target);
        }
        if (target instanceof Number) {
            return compare(((Number) target), source);
        }

        if (source instanceof Date) {
            return compare(((Date) source), target);
        }

        if (target instanceof Date) {
            return compare(((Date) target), source);
        }

        if (source instanceof String) {
            return compare(((String) source), target);
        }

        if (target instanceof String) {
            return compare(((String) target), source);
        }
        if (source instanceof Collection) {
            return compare(((Collection) source), target);
        }

        if (target instanceof Collection) {
            return compare(((Collection) target), source);
        }

        if (source instanceof Map) {
            return compare(((Map) source), target);
        }

        if (target instanceof Map) {
            return compare(((Map) target), source);
        }

        if (source.getClass().isEnum()) {
            return compare(((Enum) source), target);
        }

        if (target.getClass().isEnum()) {
            return compare(((Enum) target), source);
        }

        if (source.getClass().isArray()) {
            return compare(((Object[]) source), target);
        }

        if (target.getClass().isArray()) {
            return compare(((Object[]) target), source);
        }


        return false;

    }

    public static boolean compare(Map collection, Object target) {


        return false;
    }

    public static boolean compare(Collection collection, Object target) {

        int size = collection.size();

        Collection targetCollection = null;
        if (target instanceof String) {
            target = ((String) target).split("[, ;]");
        }
        if (target instanceof Collection) {
            targetCollection = ((Collection) target);
        } else if (target.getClass().isArray()) {
            targetCollection = Arrays.asList(((Object[]) target));
        }
        if (targetCollection == null) {
            return false;
        }
        if (size != targetCollection.size()) {
            return false;
        }


        //[1,2,3] ,[3,3,3]
       // return targetCollection.stream().allMatch(object -> {});

        return false;
    }

    public static boolean compare(Object[] number, Object target) {


        return compare(Arrays.asList(number), target);
    }

    public static boolean compare(Number number, Object target) {
        if (target.equals(number)) {
            return true;
        }
        if (target instanceof Number) {
            return number.doubleValue() == ((Number) target).doubleValue();
        }
        if (target instanceof Date) {
            return number.longValue() == ((Date) target).getTime();
        }
        if (target instanceof String) {
            //日期格式的字符串?
            String stringValue = String.valueOf(target);
            if (DateFormatter.isSupport(stringValue)) {
                //格式化为相同格式的字符串进行对比
                DateFormatter dateFormatter = DateFormatter.getFormatter(stringValue);
                return (dateFormatter.toString(new Date(number.longValue())).equals(stringValue));
            }
            if (StringUtils.isNumber(target)) {
                return new BigDecimal(stringValue).doubleValue() == number.doubleValue();
            }
        }

        return false;
    }

    public static boolean compare(Enum e, Object target) {
        String stringValue = String.valueOf(target);
        if (e instanceof EnumDict) {
            EnumDict dict = ((EnumDict) e);
            return e.name().equalsIgnoreCase(stringValue) || dict.eq(target);
        }

        return e.name().equalsIgnoreCase(stringValue);
    }

    public static boolean compare(String string, Object target) {

        if (string.equals(String.valueOf(target))) {
            return true;
        }

        if (target instanceof Date) {
            Date date = DateFormatter.fromString(string);
            return (date != null && ((Date) target).getTime() == date.getTime());
        }

        return false;
    }

    public static boolean compare(Date date, Object target) {
        if (target instanceof Date) {
            return date.getTime() == ((Date) target).getTime();
        }

        if (target instanceof String) {
            //日期格式的字符串?
            String stringValue = String.valueOf(target);
            if (DateFormatter.isSupport(stringValue)) {
                //格式化为相同格式的字符串进行对比
                DateFormatter dateFormatter = DateFormatter.getFormatter(stringValue);
                return (dateFormatter.toString(date).equals(stringValue));
            }
        }

        if (target instanceof Number) {
            long longValue = ((Number) target).longValue();
            return date.getTime() == longValue;
        }

        return false;
    }

}
