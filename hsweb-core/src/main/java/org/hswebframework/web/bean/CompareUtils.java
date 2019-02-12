package org.hswebframework.web.bean;

import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.dict.EnumDict;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("all")
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


        return compare(FastBeanCopier.copy(source, HashMap.class), FastBeanCopier.copy(target, HashMap.class));

    }

    public static boolean compare(Map<?, ?> map, Object target) {
        if (map == target) {
            return true;
        }

        if (map == null || target == null) {
            return false;
        }
        Map<?, ?> targetMap = null;
        if (target instanceof Map) {
            targetMap = ((Map) target);
        } else {
            targetMap = FastBeanCopier.copy(target, HashMap::new);
        }

        if (map.size() != targetMap.size()) {
            return false;
        }
        for (Map.Entry entry : map.entrySet()) {
            if (!compare(entry.getValue(), targetMap.get(entry.getKey()))) {
                return false;
            }
        }

        return true;
    }


    public static boolean compare(Collection collection, Object target) {
        if (collection == target) {
            return true;
        }

        if (collection == null || target == null) {
            return false;
        }
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

        Set left = new HashSet(collection);
        Set right = new HashSet(targetCollection);

        if (left.size() < right.size()) {
            Set tmp = right;
            right = left;
            left = tmp;
        }
        l:
        for (Object source : left) {
            if (!right.stream().anyMatch(targetObj -> compare(source, targetObj))) {
                return false;
            }
        }
        return true;
    }

    public static boolean compare(Object[] number, Object target) {


        return compare(Arrays.asList(number), target);
    }

    public static boolean compare(Number number, Object target) {
        if (number == target) {
            return true;
        }

        if (number == null || target == null) {
            return false;
        }

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
            try{
                return new BigDecimal(stringValue).doubleValue() == number.doubleValue();
            }catch (NumberFormatException e){
                return false;
            }
        }

        return false;
    }

    public static boolean compare(Enum e, Object target) {
        if (e == target) {
            return true;
        }

        if (e == null || target == null) {
            return false;
        }
        String stringValue = String.valueOf(target);
        if (e instanceof EnumDict) {
            EnumDict dict = ((EnumDict) e);
            return e.name().equalsIgnoreCase(stringValue) || dict.eq(target);
        }

        return e.name().equalsIgnoreCase(stringValue);
    }

    public static boolean compare(String string, Object target) {
        if (string == target) {
            return true;
        }

        if (string == null || target == null) {
            return false;
        }
        if (string.equals(String.valueOf(target))) {
            return true;
        }

        if (target instanceof Enum) {
            return compare(((Enum) target), string);
        }

        if (target instanceof Date) {
            return compare(((Date) target), string);
        }

        if (target instanceof Number) {
            return compare(((Number) target), string);
        }
        if (target instanceof Collection) {
            return compare(((Collection) target), string);
        }

        return false;
    }

    public static boolean compare(Date date, Object target) {
        if (date == target) {
            return true;
        }

        if (date == null || target == null) {
            return false;
        }
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
