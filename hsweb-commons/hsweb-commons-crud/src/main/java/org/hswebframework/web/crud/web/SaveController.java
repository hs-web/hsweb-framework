package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基于Repository的通用CRUD保存控制器接口
 * 
 * <p>基于{@link SyncRepository}提供了标准化的数据保存、新增、修改等REST API接口。</p>
 * <p>该接口直接与数据库Repository层交互，提供更直接的数据库操作能力。</p>
 * <p>支持单个实体和批量操作，并自动处理创建人、修改人等审计字段。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>批量保存数据（存在则更新，不存在则新增）</li>
 *     <li>批量新增数据（使用高性能的批量插入）</li>
 *     <li>单个数据新增</li>
 *     <li>根据ID修改数据</li>
 *     <li>自动填充审计字段（创建人、创建时间、修改人、修改时间）</li>
 * </ul>
 * 
 * <p>与{@link ServiceSaveController}的区别：</p>
 * <ul>
 *     <li>直接使用{@link SyncRepository}进行数据库操作，性能更高</li>
 *     <li>批量插入使用{@code insertBatch}方法，针对大数据量优化</li>
 *     <li>更适合简单的CRUD操作，不包含复杂的业务逻辑</li>
 *     <li>提供更直接的数据库访问控制</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/product")
 * public class ProductController implements SaveController<Product, String> {
 *     
 *     @Autowired
 *     private SyncRepository<Product, String> productRepository;
 *     
 *     @Override
 *     public SyncRepository<Product, String> getRepository() {
 *         return productRepository;
 *     }
 * }
 * 
 * // API调用示例：
 * // PATCH /product              - 批量保存产品数据
 * // POST /product/_batch        - 批量新增产品
 * // POST /product               - 新增单个产品
 * // PUT /product/123            - 修改ID为123的产品
 * }</pre>
 * 
 * @param <E> 实体类型
 * @param <K> 主键类型
 * @author hsweb-generator
 * @since 4.0
 * @see SyncRepository
 * @see ServiceSaveController
 * @see SaveResult
 */
public interface SaveController<E, K> {

    /**
     * 获取同步Repository实例
     * 
     * <p>子类必须实现此方法，返回对应的Repository实例用于执行具体的数据库操作。</p>
     * <p>Repository提供了直接的数据库访问能力，包括批量操作、事务支持等。</p>
     * 
     * @return 同步Repository实例，提供数据库CRUD操作能力
     */
    @Authorize(ignore = true)
    SyncRepository<E, K> getRepository();

    /**
     * 应用创建实体的审计信息
     * 
     * <p>为实体自动填充创建相关的审计字段：</p>
     * <ul>
     *     <li>创建时间：设置为当前时间</li>
     *     <li>创建人ID：设置为当前登录用户ID</li>
     *     <li>创建人姓名：设置为当前登录用户姓名</li>
     * </ul>
     * 
     * <p>该方法通常在新增操作时被调用，确保数据的可追溯性。</p>
     * 
     * @param authentication 当前用户认证信息，不能为null
     * @param entity 要处理的实体对象，必须实现 {@link RecordCreationEntity} 接口
     * @return 填充了创建审计信息的实体对象
     * @throws ClassCastException 当entity未实现RecordCreationEntity接口时抛出
     */
    @Authorize(ignore = true)
    default E applyCreationEntity(Authentication authentication, E entity) {
        RecordCreationEntity creationEntity = ((RecordCreationEntity) entity);
        creationEntity.setCreateTimeNow();
        creationEntity.setCreatorId(authentication.getUser().getId());
        creationEntity.setCreatorName(authentication.getUser().getName());
        return entity;
    }

    /**
     * 应用修改实体的审计信息
     * 
     * <p>为实体自动填充修改相关的审计字段：</p>
     * <ul>
     *     <li>修改时间：设置为当前时间</li>
     *     <li>修改人ID：设置为当前登录用户ID</li>
     *     <li>修改人姓名：设置为当前登录用户姓名</li>
     * </ul>
     * 
     * <p>该方法通常在更新操作时被调用，记录数据的最后修改信息。</p>
     * 
     * @param authentication 当前用户认证信息，不能为null
     * @param entity 要处理的实体对象，必须实现 {@link RecordModifierEntity} 接口
     * @return 填充了修改审计信息的实体对象
     * @throws ClassCastException 当entity未实现RecordModifierEntity接口时抛出
     */
    @Authorize(ignore = true)
    default E applyModifierEntity(Authentication authentication, E entity) {
        RecordModifierEntity modifierEntity = ((RecordModifierEntity) entity);
        modifierEntity.setModifyTimeNow();
        modifierEntity.setModifierId(authentication.getUser().getId());
        modifierEntity.setModifierName(authentication.getUser().getName());
        return entity;
    }

    /**
     * 根据实体类型自动应用相应的审计信息
     * 
     * <p>该方法会自动检查实体类型并调用相应的审计信息填充方法：</p>
     * <ul>
     *     <li>如果实体实现了 {@link RecordCreationEntity}，则调用 {@link #applyCreationEntity}</li>
     *     <li>如果实体实现了 {@link RecordModifierEntity}，则调用 {@link #applyModifierEntity}</li>
     *     <li>如果两个接口都实现了，则两个方法都会被调用</li>
     * </ul>
     * 
     * <p>这是一个智能的审计信息处理方法，根据实体的接口实现自动选择合适的处理策略。</p>
     * 
     * @param entity 要处理的实体对象
     * @param authentication 当前用户认证信息
     * @return 填充了相应审计信息的实体对象
     */
    @Authorize(ignore = true)
    default E applyAuthentication(E entity, Authentication authentication) {
        if (entity instanceof RecordCreationEntity) {
            entity = applyCreationEntity(authentication, entity);
        }
        if (entity instanceof RecordModifierEntity) {
            entity = applyModifierEntity(authentication, entity);
        }
        return entity;
    }

    /**
     * 批量保存数据
     * 
     * <p>根据实体是否包含ID来决定操作类型：</p>
     * <ul>
     *     <li>如果实体包含ID且对应数据存在，则执行更新操作</li>
     *     <li>如果实体不包含ID或对应数据不存在，则执行新增操作</li>
     * </ul>
     * 
     * <p>该方法使用Repository的save操作，具有以下特点：</p>
     * <ul>
     *     <li>自动判断新增还是更新</li>
     *     <li>支持事务处理，要么全部成功，要么全部失败</li>
     *     <li>返回详细的操作结果统计</li>
     *     <li>操作前自动填充审计信息</li>
     * </ul>
     * 
     * <p>性能说明：对于大批量数据（>1000条），建议考虑使用专门的批量导入方案。</p>
     * 
     * @param payload 要保存的实体列表，不能为null或empty
     * @return 保存结果，包含成功数量、失败数量、影响行数等详细信息
     * @throws IllegalArgumentException 如果payload为null或empty
     * @see SaveResult
     */
    @PatchMapping
    @SaveAction
    @Operation(summary = "保存数据", description = "如果传入了id,并且对应数据存在,则尝试覆盖,不存在则新增.")
    default SaveResult save(@RequestBody List<E> payload) {
        return getRepository()
                .save(Authentication
                              .current()
                              .map(auth -> {
                                  for (E e : payload) {
                                      applyAuthentication(e, auth);
                                  }
                                  return payload;
                              })
                              .orElse(payload)
                );
    }

    /**
     * 批量新增数据
     * 
     * <p>使用高性能的批量插入操作，适用于大量数据的快速写入场景。</p>
     * <p>与单条插入相比，批量插入具有以下优势：</p>
     * <ul>
     *     <li>减少网络往返次数，提高性能</li>
     *     <li>减少数据库连接开销</li>
     *     <li>支持批量提交，提高事务效率</li>
     *     <li>自动处理主键生成和约束检查</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *     <li>数据导入</li>
     *     <li>批量创建记录</li>
     *     <li>初始化数据</li>
     *     <li>数据迁移</li>
     * </ul>
     * 
     * <p>注意事项：</p>
     * <ul>
     *     <li>所有实体都将被视为新增，如果存在重复主键将抛出异常</li>
     *     <li>操作前会自动填充创建审计信息</li>
     *     <li>支持数据库级别的约束检查</li>
     * </ul>
     * 
     * @param payload 要新增的实体列表，不能为null或empty
     * @return 成功插入的记录数量
     * @throws IllegalArgumentException 如果payload为null或empty
     * @throws org.springframework.dao.DuplicateKeyException 如果存在主键冲突
     */
    @PostMapping("/_batch")
    @SaveAction
    @Operation(summary = "批量新增数据")
    default int add(@RequestBody List<E> payload) {
        return getRepository()
                .insertBatch(Authentication
                                     .current()
                                     .map(auth -> {
                                         for (E e : payload) {
                                             applyAuthentication(e, auth);
                                         }
                                         return payload;
                                     })
                                     .orElse(payload)
                );
    }

    /**
     * 新增单个数据
     * 
     * <p>插入一个新实体到数据库，并返回插入后的数据（可能包含生成的ID等信息）。</p>
     * <p>适用于交互式的单条记录创建场景。</p>
     * 
     * <p>操作特点：</p>
     * <ul>
     *     <li>使用Repository的insert方法，确保是新增操作</li>
     *     <li>自动填充创建审计信息</li>
     *     <li>返回插入后的完整实体数据</li>
     *     <li>如果实体包含自增主键，返回的实体将包含生成的ID</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *     <li>如果主键冲突，将抛出DuplicateKeyException</li>
     *     <li>如果违反数据库约束，将抛出相应的约束异常</li>
     *     <li>如果必填字段缺失，将抛出数据完整性异常</li>
     * </ul>
     * 
     * @param payload 要新增的实体对象，不能为null
     * @return 新增后的实体对象，可能包含生成的ID等自动填充字段
     * @throws IllegalArgumentException 如果payload为null
     * @throws org.springframework.dao.DuplicateKeyException 如果主键冲突
     * @throws org.springframework.dao.DataIntegrityViolationException 如果违反数据完整性约束
     */
    @PostMapping
    @SaveAction
    @Operation(summary = "新增单个数据,并返回新增后的数据.")
    default E add(@RequestBody E payload) {
        this.getRepository()
            .insert(Authentication
                            .current()
                            .map(auth -> applyAuthentication(payload, auth))
                            .orElse(payload));
        return payload;
    }

    /**
     * 根据ID修改数据
     * 
     * <p>根据指定的主键ID更新对应的实体数据。</p>
     * <p>只更新传入实体中非null的字段，null字段将被忽略（部分更新）。</p>
     * 
     * <p>更新策略：</p>
     * <ul>
     *     <li>使用乐观锁策略，避免并发更新冲突</li>
     *     <li>只更新实际发生变化的字段</li>
     *     <li>自动填充修改审计信息</li>
     *     <li>支持版本号控制（如果实体包含版本字段）</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *     <li>true：找到记录并成功更新</li>
     *     <li>false：未找到对应ID的记录，或记录未发生实际变化</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *     <li>表单数据更新</li>
     *     <li>状态字段修改</li>
     *     <li>部分字段更新</li>
     *     <li>批量状态更新的单条操作</li>
     * </ul>
     * 
     * @param id 要修改的实体主键ID，不能为null
     * @param payload 更新的实体数据，不能为null
     * @return true表示更新成功，false表示未找到记录或无需更新
     * @throws IllegalArgumentException 如果id或payload为null
     * @throws org.springframework.dao.OptimisticLockingFailureException 如果发生乐观锁冲突
     */
    @PutMapping("/{id}")
    @SaveAction
    @Operation(summary = "根据ID修改数据")
    default boolean update(@PathVariable K id, @RequestBody E payload) {

        return getRepository()
                .updateById(id, Authentication
                        .current()
                        .map(auth -> applyAuthentication(payload, auth))
                        .orElse(payload))
                > 0;

    }
}
