package org.hswebframework.web.bean;

import lombok.Data;

import java.util.*;

@Data
public class Source {
    private String name;

    private String[] ids;

    private boolean boy;

    private Boolean boy2;

    private boolean boy3;

    private int age;

    private Integer age2;

    private int age3;

    private Date deleteTime = new Date();

    private Date createTime = new Date();

    private String updateTime = "2018-01-01";


    private NestObject nestObject;

    private List<NestObject> nestObjects = Arrays.asList(new NestObject("test", 1, "1234567"), new NestObject("test", 1, "1234567"));

    private Map<String, Object> nestObject2 = new HashMap<>();

    private NestObject nestObject3 = new NestObject("test", 1, "1234567");

    private Color color = Color.RED;

    private String color2 = "红色";

    private int color3 = Color.BLUE.getValue();

    private List<String> arr = Arrays.asList("2", "3");

    private List<String> arr4 = Arrays.asList("2", "3");

    private String[] arr2 = {"1", "2"};

    private String[] arr3 = {"1", "2"};

    private String[] arr5 = {"1", "2"};

    private String[] arr6 = {"1", "2"};


}


