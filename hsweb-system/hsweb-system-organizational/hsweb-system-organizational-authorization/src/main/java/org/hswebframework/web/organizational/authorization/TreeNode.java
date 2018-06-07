package org.hswebframework.web.organizational.authorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 树形结构节点信息
 *
 * @param <V> 节点值类型
 * @author zhouhao
 * @since 3.0
 */
public class TreeNode<V> implements Serializable {
    private static final long serialVersionUID = 1_0;

//    /**
//     * 父节点,根节点为{@code null}
//     */
//    private TreeNode<V> parent;

    /**
     * 节点值
     */
    private V value;

    /**
     * 节点层级
     */
    private int level;

    private Set<TreeNode<V>> children;
//
//    public TreeNode<V> getParent() {
//        return parent;
//    }
//
//    public void setParent(TreeNode<V> parent) {
//        this.parent = parent;
//    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public Set<TreeNode<V>> getChildren() {
        return children;
    }

    public void setChildren(Set<TreeNode<V>> children) {
        this.children = children;
        children.forEach(node -> node.setLevel(getLevel() + 1));
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<V> getAllValue() {
        List<V> values = new ArrayList<>(children != null ? children.size() + 1 : 1);
        values.add(value);
        if (null != children) {
            children.stream().map(TreeNode::getAllValue).flatMap(List::stream).forEach(values::add);
        }
        return values;
    }

    @Override
    public int hashCode() {
        if (value != null) {
            return value.hashCode();
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeNode) {
            return obj.hashCode() == hashCode();
        }
        return false;
    }

    public List<V> getAllValue(Predicate<TreeNode<V>> filter) {
        List<V> values = new ArrayList<>(getChildren().size() + 1);
        if (filter.test(this)) {
            values.add(value);
        }
        children.stream().filter(filter).map(val -> val.getAllValue(filter)).flatMap(List::stream).forEach(values::add);
        return values;
    }
}
