package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.hswebframework.web.api.crud.entity.QueryOperation;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.hswebframework.web.crud.service.TreeSortEntityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 树形结构CRUD查询控制器接口
 *
 * <p>专门用于处理具有树形结构的实体数据查询，提供了标准化的树形数据查询REST API。</p>
 * <p>支持多种树形数据查询方式：树形结构查询、子节点查询、子节点树形结构查询等。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>查询数据并转换为树形结构</li>
 *     <li>查询包含所有子节点的平铺数据</li>
 *     <li>查询子节点并转换为树形结构</li>
 *     <li>支持GET和POST两种请求方式</li>
 *     <li>支持动态查询条件</li>
 * </ul>
 *
 * <p>树形结构说明：</p>
 * <ul>
 *     <li>实体必须实现 {@link TreeSortSupportEntity} 接口</li>
 *     <li>具备父子关系字段（如parentId）</li>
 *     <li>支持排序字段（如sortIndex）</li>
 *     <li>自动处理父子关系的层级结构</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/menu")
 * public class MenuController implements TreeServiceQueryController<Menu, String> {
 *
 *     @Autowired
 *     private MenuService menuService;
 *
 *     @Override
 *     public TreeSortEntityService<Menu, String> getService() {
 *         return menuService;
 *     }
 * }
 *
 * // API调用示例：
 * // GET /menu/_query/tree                        - 获取完整菜单树
 * // GET /menu/_query/_children?filter[id]=root     - 获取指定节点的所有子节点
 * // POST /menu/_query/_children/tree             - POST方式获取子节点树形结构
 * }</pre>
 *
 * @param <E> 树形结构实体类型，必须继承 {@link TreeSortSupportEntity}
 * @param <K> 主键类型
 * @author hsweb-generator
 * @since 4.0
 * @see TreeSortSupportEntity
 * @see TreeSortEntityService
 * @see QueryParamEntity
 */
public interface TreeServiceQueryController<E extends TreeSortSupportEntity<K>, K> {

    /**
     * 获取树形结构服务实例
     *
     * <p>子类必须实现此方法，返回对应的树形结构服务实例用于执行具体的查询操作。</p>
     *
     * @return 树形结构服务实例，提供树形数据查询能力
     */
    @Authorize(ignore = true)
    TreeSortEntityService<E, K> getService();

    /**
     * GET方式查询并返回树形结构
     *
     * <p>根据查询条件查询数据，并将结果组织成树形结构返回。</p>
     * <p>会根据实体的父子关系字段自动构建层级结构，顶级节点作为根节点。</p>
     *
     * <p>适用场景：</p>
     * <ul>
     *     <li>菜单树查询</li>
     *     <li>组织架构树查询</li>
     *     <li>分类目录树查询</li>
     *     <li>权限资源树查询</li>
     * </ul>
     *
     * <p>URL示例：</p>
     * <pre>
     *     GET /_query/tree                                    // 查询所有数据的树形结构
     *     GET /_query/tree?where=status eq 1                 // 查询启用状态的树形结构
     *     GET /_query/tree?orderBy=sortIndex asc             // 按排序字段查询树形结构
     * </pre>
     *
     * <p>返回结构特点：</p>
     * <ul>
     *     <li>保持父子关系的层级结构</li>
     *     <li>子节点包含在父节点的children字段中</li>
     *     <li>按sortIndex或其他排序字段排序</li>
     *     <li>只包含符合查询条件的节点</li>
     * </ul>
     *
     * @param param 查询条件参数，通过URL参数自动绑定
     * @return 树形结构的数据列表，根节点在顶层
     * @see TreeSortEntityService#queryResultToTree(QueryParamEntity)
     */
    @GetMapping("/_query/tree")
    @QueryAction
    @QueryOperation(summary = "使用GET动态查询并返回树形结构")
    default List<E> findAllTree(@Parameter(hidden = true) QueryParamEntity param) {
        return getService().queryResultToTree(param);
    }

    /**
     * GET方式查询包含所有子节点的数据
     *
     * <p>根据查询条件查询数据，同时包含这些节点的所有子节点（递归查询）。</p>
     * <p>返回的是平铺的列表结构，不是树形结构，但包含了完整的父子关系数据。</p>
     *
     * <p>适用场景：</p>
     * <ul>
     *     <li>需要获取某个分类及其所有子分类的场景</li>
     *     <li>权限检查时需要包含子权限的场景</li>
     *     <li>删除父节点时需要同时删除子节点的场景</li>
     *     <li>统计某个部门及其下属部门的数据</li>
     * </ul>
     *
     * <p>URL示例：</p>
     * <pre>
     *     GET /_query/_children                               // 查询所有节点及其子节点
     *     GET /_query/_children?parentId=root                 // 查询指定父节点及其所有子节点
     *     GET /_query/_children?where=name like 技术%         // 查询名称匹配的节点及其子节点
     * </pre>
     *
     * <p>查询逻辑：</p>
     * <ol>
     *     <li>首先根据查询条件查询出符合条件的节点</li>
     *     <li>然后递归查询这些节点的所有子节点</li>
     *     <li>合并结果并返回平铺列表</li>
     * </ol>
     *
     * @param param 查询条件参数，支持parentId等树形结构相关条件
     * @return 包含所有子节点的平铺列表数据
     * @see TreeSortEntityService#queryIncludeChildren(QueryParamEntity)
     */
    @GetMapping("/_query/_children")
    @QueryAction
    @QueryOperation(summary = "使用GET动态查询并返回子节点数据")
    default List<E> findAllChildren(@Parameter(hidden = true) QueryParamEntity param) {
        return getService().queryIncludeChildren(param);
    }

    /**
     * GET方式查询子节点并返回树形结构
     *
     * <p>结合了 {@link #findAllChildren} 和 {@link #findAllTree} 的功能。</p>
     * <p>先查询包含所有子节点的数据，然后将结果组织成树形结构返回。</p>
     *
     * <p>适用场景：</p>
     * <ul>
     *     <li>懒加载树形结构：点击节点时加载其子树</li>
     *     <li>部分树形结构展示：只展示某个分支的完整结构</li>
     *     <li>权限控制的树形菜单：只显示有权限的菜单子树</li>
     *     <li>分类管理：展示某个分类下的完整子分类树</li>
     * </ul>
     *
     * <p>URL示例：</p>
     * <pre>
     *     GET /_query/_children/tree?parentId=dept001         // 获取指定部门的完整子部门树
     *     GET /_query/_children/tree?where=level gt 2        // 获取3级以下的树形结构
     * </pre>
     *
     * <p>与 {@link #findAllTree} 的区别：</p>
     * <ul>
     *     <li>findAllTree：查询符合条件的节点并组织成树</li>
     *     <li>findAllChildrenTree：查询符合条件的节点及其所有子节点，然后组织成树</li>
     * </ul>
     *
     * @param param 查询条件参数，通常包含parentId等父节点标识
     * @return 包含所有子节点的树形结构数据
     * @see TreeSortEntityService#queryIncludeChildrenTree(QueryParamEntity)
     */
    @GetMapping("/_query/_children/tree")
    @QueryAction
    @QueryOperation(summary = "使用GET动态查询并返回子节点树形结构数据")
    default List<E> findAllChildrenTree(@Parameter(hidden = true) QueryParamEntity param) {
        return getService().queryIncludeChildrenTree(param);
    }

    /**
     * POST方式查询并返回树形结构
     *
     * <p>功能与 {@link #findAllTree} 完全相同，但支持通过POST请求体传递复杂查询条件。</p>
     * <p>适用于查询条件复杂、URL过长或包含特殊字符的场景。</p>
     *
     * <p>请求体示例：</p>
     * <pre>
     *     POST /_query/tree
     *     Content-Type: application/json
     *
     *     {
     *         "terms": [
     *             {
     *                 "column": "status",
     *                 "termType": "eq",
     *                 "value": 1
     *             },
     *             {
     *                 "column": "type",
     *                 "termType": "in",
     *                 "value": ["menu", "button"]
     *             }
     *         ],
     *         "orderBy": "sortIndex asc,id desc"
     *     }
     * </pre>
     *
     * @param param 查询条件对象，通过请求体传递
     * @return 树形结构的数据列表
     * @see #findAllTree(QueryParamEntity)
     */
    @PostMapping("/_query/tree")
    @QueryAction
    @Operation(summary = "使用POST动态查询并返回树形结构")
    default List<E> findAllTreePost(@RequestBody QueryParamEntity param) {
        return getService().queryResultToTree(param);
    }

    /**
     * POST方式查询包含所有子节点的数据
     *
     * <p>功能与 {@link #findAllChildren} 完全相同，但支持通过POST请求体传递复杂查询条件。</p>
     * <p>适用于需要复杂条件查询子节点数据的场景。</p>
     *
     * <p>请求体示例：</p>
     * <pre>
     *     POST /_query/_children
     *     Content-Type: application/json
     *
     *     {
     *         "terms": [
     *             {
     *                 "column": "parentId",
     *                 "termType": "eq",
     *                 "value": "root"
     *             }
     *         ],
     *         "includes": ["id", "name", "parentId", "children"]
     *     }
     * </pre>
     *
     * @param param 查询条件对象，包含复杂的树形查询条件
     * @return 包含所有子节点的平铺列表数据
     * @see #findAllChildren(QueryParamEntity)
     */
    @PostMapping("/_query/_children")
    @QueryAction
    @Operation(summary = "使用POST动态查询并返回子节点数据")
    default List<E> findAllChildrenPost(@RequestBody QueryParamEntity param) {
        return getService().queryIncludeChildren(param);
    }

    /**
     * POST方式查询子节点并返回树形结构
     *
     * <p>功能与 {@link #findAllChildrenTree} 完全相同，但支持通过POST请求体传递复杂查询条件。</p>
     * <p>是最完整的树形查询API，既支持复杂条件，又包含子节点，还组织成树形结构。</p>
     *
     * <p>请求体示例：</p>
     * <pre>
     *     POST /_query/_children/tree
     *     Content-Type: application/json
     *
     *     {
     *         "terms": [
     *             {
     *                 "column": "parentId",
     *                 "termType": "eq",
     *                 "value": "system"
     *             },
     *             {
     *                 "column": "visible",
     *                 "termType": "eq",
     *                 "value": true
     *             }
     *         ],
     *         "orderBy": "sortIndex asc",
     *         "excludes": ["createTime", "updateTime"]
     *     }
     * </pre>
     *
     * <p>性能提示：</p>
     * <ul>
     *     <li>对于深层次的树形结构，建议增加适当的查询条件以限制结果集大小</li>
     *     <li>可以通过includes/excludes字段控制返回的字段，提升查询性能</li>
     *     <li>合理使用parentId条件可以避免查询整个树形结构</li>
     * </ul>
     *
     * @param param 查询条件对象，支持复杂的树形查询场景
     * @return 包含所有子节点的树形结构数据
     * @see #findAllChildrenTree(QueryParamEntity)
     */
    @PostMapping("/_query/_children/tree")
    @QueryAction
    @Operation(summary = "使用POST动态查询并返回子节点树形结构数据")
    default List<E> findAllChildrenTreePost(@RequestBody QueryParamEntity param) {
        return getService().queryIncludeChildrenTree(param);
    }

}
