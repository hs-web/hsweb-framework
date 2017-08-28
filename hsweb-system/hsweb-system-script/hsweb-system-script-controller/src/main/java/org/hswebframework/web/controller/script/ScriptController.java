package org.hswebframework.web.controller.script;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.script.ScriptEntity;
import org.hswebframework.web.logging.AccessLogger;
import  org.hswebframework.web.service.script.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  动态脚本
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.script:script}")
@Authorize(permission = "script")
@AccessLogger("动态脚本")
public class ScriptController implements SimpleGenericEntityController<ScriptEntity, String, QueryParamEntity> {

    private ScriptService scriptService;
  
    @Autowired
    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }
  
    @Override
    public ScriptService getService() {
        return scriptService;
    }
}
