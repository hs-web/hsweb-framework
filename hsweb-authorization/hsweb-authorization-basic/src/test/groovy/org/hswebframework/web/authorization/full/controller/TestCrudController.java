package org.hswebframework.web.authorization.full.controller;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.full.controller.model.TestModel;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 3.0.2
 */
@RequestMapping("/test")
@RestController
@Authorize(permission = "test")
public class TestCrudController implements CrudController<TestModel> {

    @Override
    public ResponseMessage<TestModel> delete(@PathVariable String id) {

        return ResponseMessage.ok();
    }
}
