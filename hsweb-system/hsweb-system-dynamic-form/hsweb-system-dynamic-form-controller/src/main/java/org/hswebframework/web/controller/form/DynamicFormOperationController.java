package org.hswebframework.web.controller.form;

import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动态表单常用操作控制器,如增删改查
 *
 * @author zhouhao
 * @since 3.0
 */
@RestController
@AccessLogger("动态表单操作")
@RequestMapping("/dynamic/form/operation")
public class DynamicFormOperationController {

    private DynamicFormOperationService dynamicFormOperationService;

    @Autowired
    public void setDynamicFormOperationService(DynamicFormOperationService dynamicFormOperationService) {
        this.dynamicFormOperationService = dynamicFormOperationService;
    }

}
