package org.hsweb.web.service.template;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.bean.po.template.Template;
import org.hsweb.web.service.GenericService;
import org.hsweb.web.service.form.DynamicFormService;

import java.util.List;

public interface TemplateService extends GenericService<Template, String> {

    String createNewVersion(String oldVersionId) throws Exception;

    List<Template> selectLatestList(QueryParam param) throws Exception;

    int countLatestList(QueryParam param) throws Exception;

    void deploy(String id) throws Exception;

    void unDeploy(String id) throws Exception;

    Template selectLatest(String name) throws Exception;

    Template selectByVersion(String name, int version) throws Exception;

    Template selectUsing(String name) throws Exception;

    Template selectDeploy(String name) throws Exception;

}
