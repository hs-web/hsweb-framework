package org.hsweb.web.controller.form;

import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.logger.annotation.AccessLogger;
import org.hsweb.web.authorize.annotation.Authorize;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.message.ResponseMessage;
import org.hsweb.web.service.form.FormService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 自定义表单控制器，继承自GenericController,使用rest+json
 * Created by generator(by 周浩) 2015-8-1 16:31:30
 */
@RestController
@RequestMapping(value = "/form")
@AccessLogger("表单管理")
@Authorize(module = "form")
public class FormController extends GenericController<Form, String> {

    //默认服务类
    @Resource
    private FormService formService;

    @Override
    public FormService getService() {
        return this.formService;
    }

    /**
     * 查询最新版本的表单列表
     */
    @RequestMapping(value = "/~latest", method = RequestMethod.GET)
    public ResponseMessage latestList(QueryParam param) {
        ResponseMessage message;
        try {
            if (!param.isPaging()) {
                message = new ResponseMessage(true, formService.selectLatestList(param));
            } else {
                param.setPaging(false);
                int total = formService.countLatestList(param);
                param.rePaging(total);
                List<Form> list = formService.selectLatestList(param);
                PagerResult<Form> result = new PagerResult<>();
                result.setData(list).setTotal(total);
                message = new ResponseMessage(true, result);
            }
            message.include(Form.class, param.getIncludes())
                    .exclude(Form.class, param.getExcludes())
                    .onlyData();
        } catch (Exception e) {
            message = new ResponseMessage(false, e);
        }
        return message;
    }

    @RequestMapping(value = "/{id}/deploy", method = RequestMethod.PUT)
    public ResponseMessage deploy(@PathVariable("id") String id) {
        try {
            formService.deploy(id);
            return new ResponseMessage(true, "success");
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    @RequestMapping(value = "/{id}/unDeploy", method = RequestMethod.PUT)
    public ResponseMessage unDeploy(@PathVariable("id") String id) {
        try {
            formService.unDeploy(id);
            return new ResponseMessage(true, "success");
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    @RequestMapping(value = "/{name}/html", method = RequestMethod.GET)
    public ResponseMessage html(@PathVariable("name") String name) {
        try {
            return new ResponseMessage(true, formService.createDeployHtml(name));
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    @RequestMapping(value = "/{id}/view", method = RequestMethod.GET)
    public ResponseMessage view(@PathVariable("id") String id) {
        try {
            return new ResponseMessage(true, formService.createViewHtml(id));
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }
}
