package org.hswebframework.web.boost;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Target {
    private String name;
    private int   age;
    private String[] ids;
    private NestObject nestObject;

    private NestObject nestObject2;

    private Map<String,Object> nestObject3;

    private int age2;

    private boolean boy2;

    private Boolean boy;

    private String age3;

    private String boy3;

    private String color;

    private Color color2;

    private List<String> arr2;

    private String[] arr ;

    private Integer[] arr3;

    private Integer[] arr4;



}