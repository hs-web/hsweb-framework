package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryNoPagingOperation;
import org.hswebframework.web.api.crud.entity.QueryOperation;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.crud.service.CrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;

/**
 * 通用CRUD查询控制器接口
 *
 * <p>基于{@link CrudService}提供了标准化的数据查询REST API接口。</p>
 * <p>支持多种查询方式：分页查询、不分页查询、统计查询、根据ID查询等。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>GET/POST方式的分页动态查询</li>
 *     <li>GET/POST方式的不分页动态查询</li>
 *     <li>GET/POST方式的统计查询</li>
 *     <li>根据ID精确查询单个实体</li>
 *     <li>支持复杂的动态查询条件</li>
 *     <li>支持排序、分页、条件过滤</li>
 * </ul>
 *
 * <p>查询条件支持：</p>
 * <ul>
 *     <li>简单where条件：<code>where=name is 张三</code></li>
 *     <li>复杂terms条件：支持like、eq、gt、lt等多种条件类型</li>
 *     <li>排序：<code>orderBy=id desc,name asc</code></li>
 *     <li>分页：<code>pageIndex=0&pageSize=20</code></li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/user")
 * public class UserController implements ServiceQueryController<User, String> {
 *
 *     @Autowired
 *     private UserService userService;
 *
 *     @Override
 *     public CrudService<User, String> getService() {
 *         return userService;
 *     }
 * }
 *
 * // 使用示例：
 * // GET /user/_query?pageIndex=0&pageSize=10&where=name like 张%&orderBy=id desc
 * // POST /user/_query/no-paging
 * // GET /user/123
 * }</pre>
 *
 * @param <E> 实体类型
 * @param <K> 主键类型
 * @author hsweb-generator
 * @see CrudService
 * @see QueryParamEntity
 * @see PagerResult
 * @since 4.0
 */
public interface ServiceQueryController<E, K> {

    /**
     * 获取CRUD服务实例
     *
     * <p>子类必须实现此方法，返回对应的服务实例用于执行具体的查询操作。</p>
     *
     * @return CRUD服务实例
     */
    @Authorize(ignore = true)
    CrudService<E, K> getService();

    /**
     * GET方式动态查询（不返回分页总数）
     *
     * <p>执行动态查询但不计算总数，适用于不需要分页信息的场景，性能更好。</p>
     * <p>支持通过URL参数传递查询条件，参数会自动绑定到{@link QueryParamEntity}对象。</p>
     *
     * <p>URL示例：</p>
     * <pre>
     *     GET /_query/no-paging?pageIndex=0&pageSize=20&where=name is 张三&orderBy=id desc
     * </pre>
     *
     * <p>支持的查询参数：</p>
     * <ul>
     *     <li>pageIndex: 页码，从0开始</li>
     *     <li>pageSize: 每页大小</li>
     *     <li>where: 简单条件，如 "name is 张三" 或 "age gt 18"</li>
     *     <li>orderBy: 排序条件，如 "id desc" 或 "name asc,id desc"</li>
     *     <li>paging: 是否分页，设为false可获取全部数据</li>
     * </ul>
     *
     * @param query 动态查询条件，通过URL参数自动绑定
     * @return 查询结果列表，按分页参数限制数量
     * @see QueryParamEntity
     */
    @GetMapping("/_query/no-paging")
    @QueryAction
    @QueryOperation(summary = "使用GET方式分页动态查询(不返回总数)",
        description = "此操作不返回分页总数,如果需要获取全部数据,请设置参数paging=false")
    default List<E> query(@Parameter(hidden = true) QueryParamEntity query) {
        return getService()
            .createQuery()
            .setParam(query)
            .fetch();
    }

    /**
     * POST方式动态查询（不返回分页总数）
     *
     * <p>通过POST请求体传递复杂查询条件，适用于条件复杂或包含特殊字符的查询场景。</p>
     * <p>支持更丰富的查询条件配置，包括terms高级条件。</p>
     *
     * <p>请求体示例：</p>
     * <pre>
     *     POST /_query/no-paging
     *     Content-Type: application/json
     *
     *     {
     *         "pageIndex":0,
     *         "pageSize":20,
     *         "where":"name like 张%", // 简单条件，防SQL注入,不能与terms共存.
     *         "orderBy":"id desc",
     *         "terms":[ // 高级条件数组
     *             {
     *                 "column":"name",        // 字段名
     *                 "termType":"like",      // 条件类型：like,eq,gt,lt,in等
     *                 "value":"张%"          // 条件值
     *             },
     *             {
     *                 "column":"age",
     *                 "termType":"gt",
     *                 "value":18
     *             }
     *         ]
     *     }
     * </pre>
     *
     * @param query 查询条件对象，包含分页、排序、过滤条件等
     * @return 查询结果列表，不包含总数信息
     * @see QueryParamEntity
     */
    @PostMapping("/_query/no-paging")
    @QueryAction
    @Operation(summary = "使用POST方式分页动态查询(不返回总数)",
        description = "此操作不返回分页总数,如果需要获取全部数据,请设置参数paging=false")
    default List<E> postQuery(@RequestBody QueryParamEntity query) {
        return this.query(query);
    }

    /**
     * GET方式分页查询（返回分页信息）
     *
     * <p>执行分页查询并返回完整的分页信息，包括总记录数、当前页数据等。</p>
     * <p>如果查询参数中包含total字段，则使用该值作为总数，避免重复统计。</p>
     *
     * <p>URL示例：</p>
     * <pre>
     *    GET /_query?pageIndex=0&pageSize=20&where=name is 张三&orderBy=id desc
     * </pre>
     *
     * <p>性能优化：</p>
     * <ul>
     *     <li>当总数为0时，直接返回空结果，不执行数据查询</li>
     *     <li>支持传入total参数避免重复统计</li>
     *     <li>自动调整分页参数，防止超出范围</li>
     * </ul>
     *
     * @param query 查询条件，通过URL参数自动绑定
     * @return 分页查询结果，包含总数、当前页数据、分页信息等
     * @see PagerResult
     * @see QueryParamEntity
     */
    @GetMapping("/_query")
    @QueryAction
    @QueryOperation(summary = "使用GET方式分页动态查询")
    default PagerResult<E> queryPager(@Parameter(hidden = true) QueryParamEntity query) {
        if (query.getTotal() != null) {
            return PagerResult
                .of(query.getTotal(),
                    getService()
                        .createQuery()
                        .setParam(query.rePaging(query.getTotal()))
                        .fetch(), query)
                ;
        }
        int total = getService().createQuery().setParam(query.clone()).count();
        if (total == 0) {
            return PagerResult.of(0, Collections.emptyList(), query);
        }
        return PagerResult
            .of(total,
                getService()
                    .createQuery()
                    .setParam(query.rePaging(total))
                    .fetch(), query);
    }

    /**
     * POST方式分页查询（返回分页信息）
     *
     * <p>通过POST请求体传递复杂查询条件的分页查询，功能与GET方式相同。</p>
     * <p>适用于查询条件复杂、URL过长或包含特殊字符的场景。</p>
     *
     * @param query 查询条件对象，通过请求体传递
     * @return 分页查询结果，包含总数、当前页数据、分页信息等
     * @see #queryPager(QueryParamEntity)
     */
    @PostMapping("/_query")
    @QueryAction
    @SuppressWarnings("all")
    @Operation(summary = "使用POST方式分页动态查询")
    default PagerResult<E> postQueryPager(@RequestBody QueryParamEntity query) {
        return queryPager(query);
    }

    /**
     * POST方式统计查询
     *
     * <p>通过POST请求体传递查询条件，只返回符合条件的记录总数。</p>
     * <p>适用于需要统计数量但不需要具体数据的场景。</p>
     *
     * @param query 查询条件对象
     * @return 符合条件的记录总数
     * @see #count(QueryParamEntity)
     */
    @PostMapping("/_count")
    @QueryAction
    @Operation(summary = "使用POST方式查询总数")
    default int postCount(@RequestBody QueryParamEntity query) {
        return this.count(query);
    }

    /**
     * GET方式统计查询
     *
     * <p>根据查询条件统计符合条件的记录总数，不返回具体数据。</p>
     * <p>适用于分页前的总数统计、数据校验等场景。</p>
     *
     * <p>URL示例：</p>
     * <pre>
     *     GET /_count?where=status eq 1&terms=[{"column":"age","termType":"gt","value":18}]
     * </pre>
     *
     * @param query 查询条件，通过URL参数自动绑定
     * @return 符合条件的记录总数，0表示无匹配记录
     */
    @GetMapping("/_count")
    @QueryAction
    @QueryNoPagingOperation(summary = "使用GET方式查询总数")
    default int count(@Parameter(hidden = true) QueryParamEntity query) {
        return getService()
            .createQuery()
            .setParam(query)
            .count();
    }

    /**
     * 根据ID查询单个实体
     *
     * <p>通过主键ID精确查询单个实体对象。</p>
     * <p>如果指定ID的记录不存在，将抛出{@link NotFoundException}异常。返回404错误</p>
     *
     * <p>URL示例：</p>
     * <pre>
     *     GET /123           // 查询ID为123的记录
     *     GET /user_001      // 查询ID为user_001的记录
     * </pre>
     *
     * <p>路径变量说明：</p>
     * <ul>
     *     <li>支持各种类型的ID：String、Long、Integer等</li>
     *     <li>路径模式 {id:.+} 支持包含特殊字符的ID</li>
     * </ul>
     *
     * @param id 实体的主键ID，不能为null
     * @return 查询到的实体对象
     * @throws NotFoundException        当指定ID的记录不存在时抛出
     * @throws IllegalArgumentException 当id参数为null时抛出
     */
    @GetMapping("/{id:.+}")
    @QueryAction
    @Operation(summary = "根据ID查询")
    default E getById(@PathVariable K id) {
        return getService()
            .findById(id)
            .orElseThrow(NotFoundException.NoStackTrace::new);
    }

}
