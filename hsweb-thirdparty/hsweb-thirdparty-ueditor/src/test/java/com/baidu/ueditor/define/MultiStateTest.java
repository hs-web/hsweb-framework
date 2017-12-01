package com.baidu.ueditor.define;

import org.junit.Test;

import static org.junit.Assert.*;

public class MultiStateTest {

    @Test
    public void toJSONString(){
        MultiState state=new MultiState(true);

        state.addState(new BaseState());
        state.putInfo("test",1);
        state.putInfo("test","1");

        System.out.println(state.toJSONString());

    }
}