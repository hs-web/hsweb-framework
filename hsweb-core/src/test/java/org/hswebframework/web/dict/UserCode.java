package org.hswebframework.web.dict;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Dict(id = "UserCode")
public enum UserCode implements EnumDict {

    SIMPLE("SIMPLE", "TEXT", "测试"),
    TEST("TEST", "TEST", "测试");


    private String value;

    private String text;

    private String comments;
}
