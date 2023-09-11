/*
 *
 *  * Copyright 2020 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.api.crud.entity;


import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 支持树结构的实体类
 *
 * @param <PK> 主键类型
 * @author zhouhao
 * @since 4.0
 */
@SuppressWarnings("all")
public interface TreeSupportEntity<PK> extends Entity {

    /**
     * 获取主键
     *
     * @return ID
     */
    PK getId();

    /**
     * 设置主键
     *
     * @param id ID
     */
    void setId(PK id);

    /**
     * 获取树路径,树路径表示当前节点所在位置
     * 格式通常为: aBcD-EfgH-iJkl,以-分割,一个分割表示一级.
     * 比如: aBcD-EfgH-iJkl表示 当前节点在第三级,上一个节点为EfgH.
     *
     * @return 树路径
     */
    String getPath();

    /**
     * 设置路径,此值通常不需要手动设置,在进行保存时，由service自动进行分配.
     *
     * @param path 路径
     * @see TreeSupportEntity#expandTree2List(TreeSupportEntity, IDGenerator)
     */
    void setPath(String path);

    /**
     * 获取上级ID
     *
     * @return 上级ID
     */
    PK getParentId();

    /**
     * 设置上级节点ID
     *
     * @param parentId
     */
    void setParentId(PK parentId);

    /**
     * 获取节点层级
     *
     * @return 节点层级
     */
    Integer getLevel();

    /**
     * 设置节点层级
     *
     * @return 节点层级
     */
    void setLevel(Integer level);

    /**
     * 获取所有子节点,默认情况下此字段只会返回null.可以使用{@link TreeSupportEntity#list2tree(Collection, BiConsumer)}将
     * 列表结构转为树形结构
     *
     * @param <T> 当前实体类型
     * @return 自己节点
     */
    <T extends TreeSupportEntity<PK>> List<T> getChildren();

    @Override
    default void tryValidate(Class<?>... groups) {
        Entity.super.tryValidate(groups);
        if (getId() != null && Objects.equals(getId(), getParentId())) {
            throw new ValidationException("parentId", "子节点ID不能与父节点ID相同");
        }
    }

    /**
     * 根据path获取父节点的path
     *
     * @param path path
     * @return 父节点path
     */
    static String getParentPath(String path) {
        if (path == null || path.length() < 4) {
            return null;
        }
        return path.substring(0, path.length() - 5);
    }

    static <T extends TreeSupportEntity> void forEach(Collection<T> list, Consumer<T> consumer) {
        Queue<T> queue = new LinkedList<>(list);
        Set<Long> all = new HashSet<>();
        for (T node = queue.poll(); node != null; node = queue.poll()) {
            long hash = System.identityHashCode(node);
            if (all.contains(hash)) {
                continue;
            }
            all.add(hash);
            consumer.accept(node);
            if (!CollectionUtils.isEmpty(node.getChildren())) {
                queue.addAll(node.getChildren());
            }
        }
    }

    static <T extends TreeSupportEntity<PK>, PK> List<T> expandTree2List(T parent, IDGenerator<PK> idGenerator) {
        List<T> list = new LinkedList<>();
        expandTree2List(parent, list, idGenerator);

        return list;
    }

    static <T extends TreeSupportEntity<PK>, PK> void expandTree2List(T parent, List<T> target, IDGenerator<PK> idGenerator) {
        expandTree2List(parent, target, idGenerator, null);
    }


    /**
     * 将树形结构转为列表结构，并填充对应的数据。<br>
     * 如树结构数据： {name:'父节点',children:[{name:'子节点1'},{name:'子节点2'}]}<br>
     * 解析后:[{id:'id1',name:'父节点',path:'<b>aoSt</b>'},{id:'id2',name:'子节点1',path:'<b>aoSt</b>-oS5a'},{id:'id3',name:'子节点2',path:'<b>aoSt</b>-uGpM'}]
     *
     * @param root        树结构的根节点
     * @param target      目标集合,转换后的数据将直接添加({@link List#add(Object)})到这个集合.
     * @param <T>         继承{@link TreeSupportEntity}的类型
     * @param idGenerator ID生成策略
     * @param <PK>        主键类型
     */
    static <T extends TreeSupportEntity<PK>, PK> void expandTree2List(T root, List<T> target, IDGenerator<PK> idGenerator, BiConsumer<T, List<T>> childConsumer) {
        //尝试设置树路径path
        if (root.getPath() == null) {
            root.setPath(RandomUtil.randomChar(4));
        }
        if (root.getPath() != null) {
            root.setLevel(root.getPath().split("[-]").length);
        }
        //尝试设置排序
        if (root instanceof SortSupportEntity) {
            SortSupportEntity sortableRoot = ((SortSupportEntity) root);
            Long index = sortableRoot.getSortIndex();
            if (null == index) {
                sortableRoot.setSortIndex(1L);
            }
        }

        //尝试设置id
        PK parentId = root.getId();
        if (parentId == null) {
            parentId = idGenerator.generate();
            root.setId(parentId);
        }

        if (CollectionUtils.isEmpty(root.getChildren())) {
            target.add(root);
            return;
        }

        //所有节点处理队列
        Queue<T> queue = new LinkedList<>();
        queue.add(root);
        //已经处理过的节点过滤器
        Set<T> filter = new HashSet<>();

        for (T parent = queue.poll(); parent != null; parent = queue.poll()) {
            if (!filter.add(parent)) {
                continue;
            }

            //处理子节点
            if (!CollectionUtils.isEmpty(parent.getChildren())) {
                long index = 1;
                for (TreeSupportEntity<PK> child : parent.getChildren()) {
                    if (child.getId() == null) {
                        child.setId(idGenerator.generate());
                    }
                    child.setParentId(parent.getId());
                    child.setPath(parent.getPath() + "-" + RandomUtil.randomChar(4));
                    child.setLevel(child.getPath().split("[-]").length);

                    //子节点排序
                    if (child instanceof SortSupportEntity && parent instanceof SortSupportEntity) {
                        SortSupportEntity sortableParent = ((SortSupportEntity) parent);
                        SortSupportEntity sortableChild = ((SortSupportEntity) child);
                        if (sortableChild.getSortIndex() == null) {
                            sortableChild.setSortIndex(sortableParent.getSortIndex() * 100 + index++);
                        }
                    }
                    queue.add((T) child);
                }
            }
            if (childConsumer != null) {
                childConsumer.accept(parent, new ArrayList<>());
            }
            target.add(parent);
        }
    }

    /**
     * 集合转为树形结构,返回根节点集合
     *
     * @param dataList      需要转换的集合
     * @param childConsumer 设置子节点回调
     * @param <N>           树节点类型
     * @param <PK>          主键类型
     * @return 树形结构集合
     */
    static <N extends TreeSupportEntity<PK>, PK> List<N> list2tree(Collection<N> dataList, BiConsumer<N, List<N>> childConsumer) {
        return list2tree(dataList, childConsumer, (Function<TreeHelper<N, PK>, Predicate<N>>) predicate -> node -> node == null || predicate
                .getNode(node.getParentId()) == null);
    }

    static <N extends TreeSupportEntity<PK>, PK> List<N> list2tree(Collection<N> dataList,
                                                                   BiConsumer<N, List<N>> childConsumer,
                                                                   Predicate<N> rootNodePredicate) {
        return list2tree(dataList, childConsumer, (Function<TreeHelper<N, PK>, Predicate<N>>) predicate -> rootNodePredicate);
    }

    /**
     * 列表结构转为树结构,并返回根节点集合
     *
     * @param dataList          数据集合
     * @param childConsumer     子节点消费接口,用于设置子节点
     * @param predicateFunction 根节点判断函数,传入helper,获取一个判断是否为跟节点的函数
     * @param <N>               元素类型
     * @param <PK>              主键类型
     * @return 根节点集合
     */
    static <N extends TreeSupportEntity<PK>, PK> List<N> list2tree(final Collection<N> dataList,
                                                                   final BiConsumer<N, List<N>> childConsumer,
                                                                   final Function<TreeHelper<N, PK>, Predicate<N>> predicateFunction) {
        return TreeUtils.list2tree(dataList,
                                   TreeSupportEntity::getId,
                                   TreeSupportEntity::getParentId,
                                   childConsumer,
                                   (helper, node) -> predicateFunction.apply(helper).test(node));
    }

    /**
     * 树结构Helper
     *
     * @param <T>  节点类型
     * @param <PK> 主键类型
     */
    interface TreeHelper<T, PK> {
        /**
         * 根据主键获取子节点
         *
         * @param parentId 节点ID
         * @return 子节点集合
         */
        List<T> getChildren(PK parentId);

        /**
         * 根据id获取节点
         *
         * @param id 节点ID
         * @return 节点
         */
        T getNode(PK id);
    }
}
