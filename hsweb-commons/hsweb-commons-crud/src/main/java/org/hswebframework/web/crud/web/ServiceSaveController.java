package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.hswebframework.web.crud.service.CrudService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通用CRUD保存控制器接口
 * 
 * <p>提供了标准化的数据保存、新增、修改等REST API接口。</p>
 * <p>该接口支持单个实体和批量操作，并自动处理创建人、修改人等审计字段。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>批量保存数据（存在则更新，不存在则新增）</li>
 *     <li>批量新增数据</li>
 *     <li>单个数据新增</li>
 *     <li>根据ID修改数据</li>
 *     <li>自动填充审计字段（创建人、创建时间、修改人、修改时间）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/user")
 * public class UserController implements ServiceSaveController<User, String> {
 *     
 *     @Autowired
 *     private UserService userService;
 *     
 *     @Override
 *     public CrudService<User, String> getService() {
 *         return userService;
 *     }
 * }
 * }</pre>
 * 
 * @param <E> 实体类型
 * @param <K> 主键类型
 * @author hsweb-generator
 * @since 4.0
 */
public interface ServiceSaveController<E, K> {

    /**
     * 获取CRUD服务实例
     * 
     * <p>子类必须实现此方法，返回对应的服务实例用于执行具体的数据操作。</p>
     * 
     * @return CRUD服务实例
     */
    @Authorize(ignore = true)
    CrudService<E, K> getService();

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
     * @param authentication 当前用户认证信息
     * @param entity 要处理的实体对象，必须实现 {@link RecordCreationEntity} 接口
     * @return 填充了创建审计信息的实体对象
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
     * @param authentication 当前用户认证信息
     * @param entity 要处理的实体对象，必须实现 {@link RecordModifierEntity} 接口
     * @return 填充了修改审计信息的实体对象
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
     * <p>该方法会检查实体是否实现了相关的审计接口，并自动调用对应的方法：</p>
     * <ul>
     *     <li>如果实体实现了 {@link RecordCreationEntity}，则调用 {@link #applyCreationEntity}</li>
     *     <li>如果实体实现了 {@link RecordModifierEntity}，则调用 {@link #applyModifierEntity}</li>
     * </ul>
     * 
     * @param entity 要处理的实体对象
     * @param authentication 当前用户认证信息
     * @return 填充了审计信息的实体对象
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
     * <p>操作前会自动为每个实体填充审计信息。</p>
     * 
     * @param payload 要保存的实体列表，不能为null
     * @return 保存结果，包含成功数量、失败数量等信息
     * @see SaveResult
     */
    @PatchMapping
    @SaveAction
    @Operation(summary = "保存数据", description = "如果传入了id,并且对应数据存在,则尝试覆盖,不存在则新增.")
    default SaveResult save(@RequestBody List<E> payload) {
        return getService()
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
     * <p>批量插入多个新实体到数据库。</p>
     * <p>操作前会自动为每个实体填充创建审计信息。</p>
     * 
     * @param payload 要新增的实体列表，不能为null或empty
     * @return 成功新增的记录数量
     * @throws IllegalArgumentException 如果payload为null或empty
     */
    @PostMapping("/_batch")
    @SaveAction
    @Operation(summary = "批量新增数据")
    default int add(@RequestBody List<E> payload) {
        return getService()
                .insert(Authentication
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
     * <p>插入一个新实体到数据库，并返回新增后的数据。</p>
     * <p>操作前会自动填充创建审计信息。</p>
     * 
     * @param payload 要新增的实体对象，不能为null
     * @return 新增后的实体对象（可能包含生成的ID等信息）
     * @throws IllegalArgumentException 如果payload为null
     */
    @PostMapping
    @SaveAction
    @Operation(summary = "新增单个数据,并返回新增后的数据.")
    default E add(@RequestBody E payload) {
        this.getService()
            .insert(Authentication
                            .current()
                            .map(auth -> applyAuthentication(payload, auth))
                            .orElse(payload));
        return payload;
    }

    /**
     * 根据ID修改数据
     * 
     * <p>根据指定的ID更新对应的实体数据。</p>
     * <p>操作前会自动填充修改审计信息。</p>
     * 
     * @param id 要修改的实体ID，不能为null
     * @param payload 更新的实体数据，不能为null
     * @return true表示修改成功，false表示未找到对应数据或修改失败
     * @throws IllegalArgumentException 如果id或payload为null
     */
    @PutMapping("/{id}")
    @SaveAction
    @Operation(summary = "根据ID修改数据")
    default boolean update(@PathVariable K id, @RequestBody E payload) {

        return getService()
                .updateById(id, Authentication
                        .current()
                        .map(auth -> applyAuthentication(payload, auth))
                        .orElse(payload))
                > 0;

    }
}
