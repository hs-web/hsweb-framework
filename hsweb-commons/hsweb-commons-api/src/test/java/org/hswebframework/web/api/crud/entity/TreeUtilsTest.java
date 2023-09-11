package org.hswebframework.web.api.crud.entity;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

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


    @Getter
    @Setter
    static class Node {
        private String id;

        private List<Node> children;
    }
}