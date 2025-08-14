package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.DeleteAction;
import org.hswebframework.web.crud.service.CrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 基于{@link CrudService}的通用删除控制器接口
 *
 * @param <E> 实体类型
 * @param <K> 主键类型
 * @author zhouhao
 * @since 3.0
 */
public interface ServiceDeleteController<E, K> {

    /**
     * @return CrudService
     * @see CrudService
     */
    @Authorize(ignore = true)
    CrudService<E, K> getService();

    /**
     * 根据ID删除数据,如果id对应的数据不存在将返回404错误.
     *
     * @param id ID
     * @return 被删除的数据
     */
    @DeleteMapping("/{id:.+}")
    @DeleteAction
    @Operation(summary = "根据ID删除", description = "如果数据不存在将返回404错误")
    default E delete(@PathVariable K id) {

        E data = getService().findById(id).orElseThrow(NotFoundException.NoStackTrace::new);

        getService().deleteById(id);

        return data;
    }
}
