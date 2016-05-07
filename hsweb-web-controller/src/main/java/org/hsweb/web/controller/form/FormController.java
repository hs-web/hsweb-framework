package org.hsweb.web.controller.form;

import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.form.FormService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseMessage latestList(QueryParam param) throws Exception {
        ResponseMessage message;
        if (!param.isPaging()) {
            message = ResponseMessage.ok(formService.selectLatestList(param));
        } else {
            param.setPaging(false);
            int total = formService.countLatestList(param);
            param.rePaging(total);
            List<Form> list = formService.selectLatestList(param);
            PagerResult<Form> result = new PagerResult<>();
            result.setData(list).setTotal(total);
            message = ResponseMessage.ok(result);
        }
        message.include(Form.class, param.getIncludes())
                .exclude(Form.class, param.getExcludes())
                .onlyData();
        return message;
    }

    @RequestMapping(value = "/{name}/latest", method = RequestMethod.GET)
    public ResponseMessage latest(@PathVariable(value = "name") String name) throws Exception {
        Form form = formService.selectLatest(name);
        if (form == null) throw new BusinessException("表单不存在", 404);
        return ResponseMessage.ok(form);
    }

    @RequestMapping(value = "/{name}/{version}", method = RequestMethod.GET)
    public ResponseMessage latest(@PathVariable(value = "name") String name,
                                  @PathVariable(value = "version") Integer version) throws Exception {
        Form form = formService.selectByVersion(name, version);
        if (form == null) throw new BusinessException("表单不存在", 404);
        return ResponseMessage.ok(form);
    }

    @RequestMapping(value = "/{id}/deploy", method = RequestMethod.PUT)
    @Authorize(action = "deploy")
    public ResponseMessage deploy(@PathVariable("id") String id) throws Exception {
        formService.deploy(id);
        return ResponseMessage.ok();
    }

    @RequestMapping(value = "/{id}/unDeploy", method = RequestMethod.PUT)
    @Authorize(action = "deploy")
    public ResponseMessage unDeploy(@PathVariable("id") String id) throws Exception {
        formService.unDeploy(id);
        return ResponseMessage.ok();
    }

    @RequestMapping(value = "/{name}/html", method = RequestMethod.GET)
    public ResponseMessage html(@PathVariable("name") String name) throws Exception {
        return ResponseMessage.ok(formService.createDeployHtml(name));
    }

    @RequestMapping(value = "/{name}/using", method = RequestMethod.GET)
    public ResponseMessage using(@PathVariable("name") String name) throws Exception {
        Form form = formService.selectUsing(name);
        if (form == null) {
            throw new BusinessException("表单不存在", 404);
        }
        return ResponseMessage.ok(form).exclude(Form.class, "html");
    }

    @RequestMapping(value = "/{id}/view", method = RequestMethod.GET)
    public ResponseMessage view(@PathVariable("id") String id) throws Exception {
        return ResponseMessage.ok(formService.createViewHtml(id));
    }
}
