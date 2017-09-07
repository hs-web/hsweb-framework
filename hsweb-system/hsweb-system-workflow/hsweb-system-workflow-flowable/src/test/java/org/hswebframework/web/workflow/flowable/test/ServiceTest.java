package org.hswebframework.web.workflow.flowable.test;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouhao on 2017/7/20.
 */
public class ServiceTest {
    public static void main(String[] args) {
        p();
    }

    public static void p(){
        String s = "[{\"name\":\"a\",\"key\":\"1\"},{\"name\":\"b\",\"key\":\"2\"},{\"name\":\"c\",\"key\":\"3\"}]";
        List<Map> list = JSON.parseArray(s,Map.class);
        System.out.println(list);
    }

    public static void s(){
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();
        Map<String, String> map3 = new HashMap<>();

        map1.put("key","1");
        map1.put("name","a");
        map2.put("key","2");
        map2.put("name","b");
        map3.put("key","3");
        map3.put("name","c");

        List<Map<String,String>> list = new ArrayList<>();

        list.add(map1);
        list.add(map2);
        list.add(map3);

        System.out.println(JSON.toJSONString(list));
    }

}