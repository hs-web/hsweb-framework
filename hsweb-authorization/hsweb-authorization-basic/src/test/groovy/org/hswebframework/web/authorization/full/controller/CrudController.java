package org.hswebframework.web.authorization.full.controller;

import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhouhao
 * @since 3.0.2
 */
public interface CrudController<T> {

    @GetMapping
    @Authorize(action = Permission.ACTION_QUERY, dataAccess = @RequiresDataAccess)
    default ResponseMessage<QueryParamEntity> query(QueryParamEntity param) {
        return ResponseMessage.ok(param);
    }

    @PutMapping
    @Authorize(action = Permission.ACTION_UPDATE, dataAccess = @RequiresDataAccess)
    default ResponseMessage<T> update(@RequestBody T entity) {
        return ResponseMessage.ok(entity);
    }

    @PostMapping
    @Authorize(action = Permission.ACTION_ADD, dataAccess = @RequiresDataAccess)
    default ResponseMessage<T> insert(@RequestBody T entity) {
        return ResponseMessage.ok(entity);
    }

    @DeleteMapping("/{id}")
    @Authorize(action = Permission.ACTION_DELETE, dataAccess = @RequiresDataAccess)
    ResponseMessage<T> delete(@PathVariable String id);


}
