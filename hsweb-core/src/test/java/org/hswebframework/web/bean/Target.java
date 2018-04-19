package org.hswebframework.web.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class Target {
    private String name;
    private String[] ids;


    private Boolean boy;
    private boolean boy2;
    private String boy3;

    private int age;

    private int age2;

    private String age3;

    private Date deleteTime=new Date();

    private String createTime;

    private Date updateTime;

    private NestObject nestObject;

    private NestObject nestObject2;

    private List<Map<String,Object>> nestObjects;

    private Map<String, Object> nestObject3;


    private int color;

    private Color color2;

    private Color color3;


    private List<String> arr2;

    private String[] arr;

    private Integer[] arr3;

    private Integer[] arr4;


}