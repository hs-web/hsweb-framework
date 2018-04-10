package org.hswebframework.web.boost;

import lombok.Data;

import java.util.*;

@Data
public class Source {
    private String name;
    private int age;
    private String[] ids;

    private boolean boy;

    private Integer age2;

    private Boolean boy2;

    private int age3;

    private boolean boy3;

    private NestObject nestObject;

    private Map<String,Object> nestObject2=new HashMap<>();

    private NestObject nestObject3;


    private Color color = Color.RED;

    private String color2 = Color.RED.name();

    private List<String> arr = Arrays.asList("2","3");

    private List<String> arr4 = Arrays.asList("2","3");

    private String[] arr2 = {"1", "2"};

    private String[] arr3 = {"1", "2"};

    private String[] arr5 = {"1", "2"};

    private String[] arr6 = {"1", "2"};



}


