package org.hswebframework.web.api.crud.entity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TreeSupportEntityTraversalTest {

    @Test
    public void visitsDistinctObjectsAndStopsAtRepeatedReferences() {
        Node root = new Node();
        Node first = new Node();
        Node second = new Node();
        if (Boolean.getBoolean("tree.test.constantIdentityHash")) {
            assertEquals(System.identityHashCode(first), System.identityHashCode(second));
        }
        // 相同业务 ID / equals 不能让请求中的另一个对象被跳过。
        root.setId("same");
        first.setId("same");
        second.setId("same");
        root.children.addAll(List.of(first, second, first));
        first.children.add(root);
        List<Node> visited = new ArrayList<>();
        TreeSupportEntity.forEach(List.of(root), visited::add);
        assertEquals(3, visited.size());
        assertSame(root, visited.get(0));
        assertSame(first, visited.get(1));
        assertSame(second, visited.get(2));
    }

    static class Node extends GenericTreeSortSupportEntity<String> {
        final List<Node> children = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T extends TreeSupportEntity<String>> List<T> getChildren() {
            return (List<T>) children;
        }
    }
}
