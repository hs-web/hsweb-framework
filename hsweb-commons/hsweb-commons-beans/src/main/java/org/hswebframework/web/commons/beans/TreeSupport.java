/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
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

package org.hswebframework.web.commons.beans;


import org.hswebframework.web.id.IDGenerator;
import org.hswebframwork.utils.RandomUtil;
import org.hswebframwork.utils.StringUtils;

import java.util.List;

public interface TreeSupport {
    String getId();

    void setId(String id);

    String getTreeCode();

    void setTreeCode(String treeCode);

    String getParentId();

    void setParentId(String parentId);

    <T extends TreeSupport> List<T> getChildren();

    static String getParentTreeCode(String treeCode) {
        if (treeCode == null || treeCode.length() < 4) return null;
        return treeCode.substring(0, treeCode.length() - 5);
    }

    /**
     * 将树形结构转为列表结构，并填充对应的数据。<br>
     * 如树结构数据： {name:'父节点',children:[{name:'子节点1'},{name:'子节点2'}]}<br>
     * 解析后:[{id:'id1',name:'父节点',treeCode:'<b>aoSt</b>'},{id:'id2',name:'子节点1',treeCode:'<b>aoSt</b>-oS5a'},{id:'id3',name:'子节点2',treeCode:'<b>aoSt</b>-uGpM'}]
     *
     * @param parent 树结构的根节点
     * @param target 目标集合,转换后的数据将直接添加({@link List#add(Object)})到这个集合.
     * @param <T>    继承{@link TreeSupport}的类型
     */
    static <T extends TreeSupport> void expandTree2List(T parent, List<T> target) {
        List<T> children = parent.getChildren();
        if (parent.getTreeCode() == null) {
            parent.setTreeCode(RandomUtil.randomChar(4));
        }
        if (children != null) {
            String pid = parent.getId();
            if (pid == null) {
                pid = IDGenerator.MD5.generate();
                parent.setId(pid);
            }
            for (int i = 0; i < children.size(); i++) {
                T child = children.get(i);
                if (child instanceof SortSupport && parent instanceof SortSupport) {
                    ((SortSupport) child).setSortIndex(StringUtils.toLong(((SortSupport) parent).getSortIndex() + "0" + (i + 1)));
                }
                child.setParentId(pid);
                child.setTreeCode(parent.getTreeCode() + "-" + RandomUtil.randomChar(4));
                target.add(child);
                expandTree2List(child, target);
            }
        }
    }
}
