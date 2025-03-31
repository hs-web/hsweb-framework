package org.hswebframework.web.api.crud.entity;

import com.google.common.collect.Collections2;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TreeUtilsTest {


    @Test
    public void testTreeToList() {

        Node node1 = new Node();
        node1.setChildren(Arrays.asList(new Node(), new Node()));

        List<Node> nodes = TreeUtils.treeToList(Collections.singletonList(node1),
                                                Node::getChildren);


        assertNotNull(nodes);
        assertEquals(3, nodes.size());

    }

    @Test
    public void testListToTree() {
        int size = 5;
        List<Node> nodes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Node node = new Node();
            node.setId(String.valueOf(i));
            node.setParenTId(i == 0 ? null : String.valueOf(i - 1));
            nodes.add(node);
        }
        // 打乱顺序
        Collections.shuffle(nodes);

        // 并发执行，并且创建新的节点
        List<Node> tree = TreeUtils
                .list2tree(Collections2.transform(nodes, e -> {
                               Node copy = new Node();
                               copy.setId(e.id);
                               copy.setParenTId(e.parenTId);
                               copy.setChildren(e.children);
                               return copy;
                           }),
                           Node::getId,
                           Node::getParenTId,
                           Node::setChildren,
                           // 自定义根节点判断
                           (helper, e) -> "2".contains(e.getId()));
        assertNotNull(tree);
        Node children = tree.get(0);
        assertNotNull(children);
        while (CollectionUtils.isNotEmpty(children.getChildren())) {
            children = children.getChildren().get(0);
        }
        assertEquals("4", children.getId());
    }

    @Getter
    @Setter
    static class Node {
        private String id;

        private String parenTId;

        private List<Node> children;
    }
}