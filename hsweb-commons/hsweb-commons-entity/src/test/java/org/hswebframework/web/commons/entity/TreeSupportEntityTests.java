package org.hswebframework.web.commons.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hswebframework.web.id.IDGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class TreeSupportEntityTests {

    @Test
    public void test() {
        MenuEntity parent = MenuEntity.builder().build();
        parent.setName("menu-1");
        parent.setId(1);
        parent.setParentId(-1);

        MenuEntity m101 = MenuEntity.builder().build();
        m101.setName("menu-101");
        m101.setId(101);
        m101.setParentId(1);

        MenuEntity m102 = MenuEntity.builder().build();
        m102.setName("menu-102");
        m102.setId(102);
        m102.setParentId(1);

        MenuEntity m10201 = MenuEntity.builder().build();
        m10201.setName("menu-10201");
        m10201.setId(10201);
        m10201.setParentId(102);

        //list转为树形结构
        List<MenuEntity> tree = TreeSupportEntity
                .list2tree(Arrays.asList(parent, m101, m102, m10201), MenuEntity::setChildren, (Predicate<MenuEntity>) menu -> menu.getParentId().equals(-1));

        Assert.assertEquals(tree.get(0).getChildren().get(0).getId(), Integer.valueOf(101));
        Assert.assertEquals(tree.get(0).getChildren().get(1).getId(), Integer.valueOf(102));

        Assert.assertEquals(tree.get(0).getChildren().get(1).getChildren().get(0).getId(), Integer.valueOf(10201));

        System.out.println(JSON.toJSONString(tree, SerializerFeature.PrettyFormat));

        List<MenuEntity> list = new ArrayList<>();

        //将树形结构展平为list
        TreeSupportEntity.expandTree2List(tree.get(0), list, () -> (int) Math.round(Math.random() * 1000000), MenuEntity::setChildren);

        System.out.println(JSON.toJSONString(list, SerializerFeature.PrettyFormat));

        Assert.assertEquals(list.size(), 4);

    }
}