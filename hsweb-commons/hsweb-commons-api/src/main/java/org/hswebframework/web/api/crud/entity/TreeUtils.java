package org.hswebframework.web.api.crud.entity;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class TreeUtils {

    /**
     * 树结构转为List
     *
     * @param nodeList List
     * @param children 子节点获取函数
     * @param <N>      节点类型
     * @return List
     */
    public static <N> List<N> treeToList(Collection<N> nodeList,
                                         Function<N, Collection<N>> children) {
        List<N> list = new ArrayList<>(nodeList.size());
        flatTree(nodeList, children, list::add);
        return list;
    }

    /**
     * 平铺树结构
     *
     * @param nodeList 树结构list
     * @param children 子节点获取函数
     * @param handler  平铺节点接收函数
     * @param <N>      节点类型
     */
    public static <N> void flatTree(Collection<N> nodeList,
                                    Function<N, Collection<N>> children,
                                    Consumer<N> handler) {
        Queue<N> queue = new LinkedList<>(nodeList);
        Set<N> distinct = new HashSet<>();

        while (!queue.isEmpty()) {
            N node = queue.poll();

            if (!distinct.add(node)) {
                continue;
            }

            Collection<N> childrenList = children.apply(node);
            if (CollectionUtils.isNotEmpty(childrenList)) {
                queue.addAll(childrenList);
            }

            handler.accept(node);
        }

    }

    /**
     * 列表结构转为树结构,并返回根节点集合.
     * <p>
     * 根节点判断逻辑: parentId为空或者对应的节点数据没有在list中
     *
     * @param dataList      数据集合
     * @param childConsumer 子节点消费接口,用于设置子节点
     * @param <N>           元素类型
     * @param <PK>          主键类型
     * @return 根节点集合
     */
    public static <N, PK> List<N> list2tree(Collection<N> dataList,
                                            Function<N, PK> idGetter,
                                            Function<N, PK> parentIdGetter,
                                            BiConsumer<N, List<N>> childConsumer) {
        return list2tree(dataList,
                         idGetter,
                         parentIdGetter,
                         childConsumer,
                         (helper, node) -> {
                             PK parentId = parentIdGetter.apply(node);
                             return ObjectUtils.isEmpty(parentId)
                                 || helper.getNode(parentId) == null;
                         });
    }

    /**
     * 列表结构转为树结构,并返回根节点集合
     *
     * @param dataList      数据集合
     * @param childConsumer 子节点消费接口,用于设置子节点
     * @param rootPredicate 根节点判断函数,传入helper,获取一个判断是否为根节点的函数
     * @param <N>           元素类型
     * @param <PK>          主键类型
     * @return 根节点集合
     */
    public static <N, PK> List<N> list2tree(Collection<N> dataList,
                                            Function<N, PK> idGetter,
                                            Function<N, PK> parentIdGetter,
                                            BiConsumer<N, List<N>> childConsumer,
                                            BiPredicate<TreeSupportEntity.TreeHelper<N, PK>, N> rootPredicate) {
        Objects.requireNonNull(dataList, "source list can not be null");
        Objects.requireNonNull(childConsumer, "child consumer can not be null");
        Objects.requireNonNull(rootPredicate, "root predicate function can not be null");
        int size = dataList.size();
        if (size == 0) {
            return new ArrayList<>(0);
        }
        // id,node
        Map<PK, N> cache = Maps.newHashMapWithExpectedSize(size);
        // parentId,children
        Map<PK, List<N>> treeCache = dataList
            .stream()
            .peek(node -> cache.put(idGetter.apply(node), node))
            .filter(e -> parentIdGetter.apply(e) != null)
            .collect(Collectors.groupingBy(parentIdGetter));

        TreeSupportEntity.TreeHelper<N, PK> helper = new TreeSupportEntity.TreeHelper<N, PK>() {
            @Override
            public List<N> getChildren(PK parentId) {
                return treeCache.get(parentId);
            }

            @Override
            public N getNode(PK id) {
                return cache.get(id);
            }
        };

        List<N> list = new ArrayList<>(treeCache.size());

        for (N node : dataList) {
            //设置每个节点的子节点
            childConsumer.accept(node, treeCache.get(idGetter.apply(node)));

            //获取根节点
            if (rootPredicate.test(helper, node)) {
                list.add(node);
            }
        }
        return list;
    }

}
