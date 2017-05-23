package organizational;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 树形结构节点信息
 *
 * @param <V> 节点值类型
 * @author zhouhao
 * @since 3.0
 */
public class TreeNode<V> implements Serializable {

    /**
     * 父节点,根节点为{@code null}
     */
    private TreeNode<V> parent;

    /**
     * 节点值
     */
    private V value;

    /**
     * 节点层级
     */
    private int level;

    private List<TreeNode<V>> children;

    public TreeNode<V> getParent() {
        return parent;
    }

    public void setParent(TreeNode<V> parent) {
        this.parent = parent;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public List<TreeNode<V>> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode<V>> children) {
        this.children = children;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<V> getAllValue() {
        List<V> values = new ArrayList<>(getChildren().size() + 1);
        values.add(value);
        children.stream().map(TreeNode::getAllValue).flatMap(List::stream).forEach(values::add);
        return values;
    }

    public List<V> getAllValue(Predicate<TreeNode<V>> filter) {
        List<V> values = new ArrayList<>(getChildren().size() + 1);
        if (filter.test(this))
            values.add(value);
        children.stream().filter(filter).map(val -> val.getAllValue(filter)).flatMap(List::stream).forEach(values::add);
        return values;
    }
}
