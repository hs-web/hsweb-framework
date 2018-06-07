package org.hswebframework.web.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hswebframework.web.bean.ToString.Feature.coverIgnoreProperty;
import static org.hswebframework.web.bean.ToString.Feature.jsonFormat;
import static org.hswebframework.web.bean.ToString.Feature.nullPropertyToEmpty;

@Getter
@Setter
public class Target {
    private String   name;
    private String[] ids;


    private Boolean boy;
    private boolean boy2;
    private String  boy3;

    private int age;

    private int age2;

    private String age3;

    private Date deleteTime = new Date();

    private String createTime;

    private Date updateTime;

    @ToString.Features({coverIgnoreProperty,jsonFormat})
    @ToString.Ignore(value = "password")
    private NestObject nestObject;

    private NestObject nestObject2;

    @ToString.Ignore(value = "password")
    private List<Map<String, Object>> nestObjects;

    @ToString.Ignore("password")
    private Map<String, Object> nestObject3;

    private int color;

    private Color color2;

    private Color color3;


    private List<String> arr2;

    private String[] arr;

    private Integer[] arr3;

    private Integer[] arr4;


    @Override
    public String toString() {
        return ToString.toString(this);
    }
}